using DiagnosticsTool.Models;
using DiagnosticsTool.Models.Requests;
using DiagnosticsTool.Models.Responses;
using DiagnosticsTool.Models.Tests;
using DiagnosticsTool.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Options;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Queries;
using OpenTraceability.Queries.Diagnostics;
using System.Text;

namespace DiagnosticsTool.Controllers;

[ApiController]
[Route("api/v1/diagnostics")]
public class DiagnosticsController : ControllerBase
{
    private readonly IHttpClientFactory _httpClientFactory;
    private readonly IDiagnosticsEnvelopeCache _cache;
    private readonly DiagnosticsToolOptions _options;
    private readonly ITestService _testService;

    public DiagnosticsController(
        IHttpClientFactory httpClientFactory,
        IDiagnosticsEnvelopeCache cache,
        IOptions<DiagnosticsToolOptions> options,
        ITestService testService)
    {
        _httpClientFactory = httpClientFactory;
        _cache = cache;
        _options = options.Value;
        _testService = testService;
    }

    [HttpPost("digitallink/epcis-url/epc")]
    public async Task<ActionResult<DiagnosticsEnvelope<ResolvedUrlResult>>> ResolveDigitalLinkEpc([FromBody] DigitalLinkEpcRequest request)
    {
        if (request == null || string.IsNullOrWhiteSpace(request.EPC))
        {
            return BadRequest("EPC is required.");
        }

        if (request.Options?.URL == null)
        {
            return BadRequest("Options.URL is required.");
        }

        if (!OpenTraceability.Models.Identifiers.EPC.TryParse(request.EPC, out var epc, out var epcErr))
        {
            return BadRequest(epcErr);
        }

        RewriteTestUrl(request.Options);

        var diagnosticsReport = new DiagnosticsReport();
        var client = _httpClientFactory.CreateClient("default");
        var url = await EPCISTraceabilityResolver.GetEPCISQueryInterfaceURL(request.Options, epc, client, diagnosticsReport);

        var envelope = new DiagnosticsEnvelope<ResolvedUrlResult>
        {
            Data = new ResolvedUrlResult { EpcisQueryInterfaceUrl = url },
            Diagnostics = diagnosticsReport
        };

        var cacheId = _cache.Add(envelope);
        Response.Headers["X-Diagnostics-Id"] = cacheId;

        return Ok(envelope);
    }

    [HttpPost("digitallink/epcis-url/pgln")]
    public async Task<ActionResult<DiagnosticsEnvelope<ResolvedUrlResult>>> ResolveDigitalLinkPgln([FromBody] DigitalLinkPglnRequest request)
    {
        if (request == null || string.IsNullOrWhiteSpace(request.PGLN))
        {
            return BadRequest("PGLN is required.");
        }

        if (request.Options?.URL == null)
        {
            return BadRequest("Options.URL is required.");
        }

        if (!PGLN.TryParse(request.PGLN, out var pgln, out var pglnErr))
        {
            return BadRequest(pglnErr);
        }

        RewriteTestUrl(request.Options);

        var diagnosticsReport = new DiagnosticsReport();
        var client = _httpClientFactory.CreateClient("default");
        var url = await EPCISTraceabilityResolver.GetEPCISQueryInterfaceURL(request.Options, pgln, client, diagnosticsReport);

        var envelope = new DiagnosticsEnvelope<ResolvedUrlResult>
        {
            Data = new ResolvedUrlResult { EpcisQueryInterfaceUrl = url },
            Diagnostics = diagnosticsReport
        };

        var cacheId = _cache.Add(envelope);
        Response.Headers["X-Diagnostics-Id"] = cacheId;

        return Ok(envelope);
    }

    [HttpPost("epcis/query/events")]
    public async Task<ActionResult<DiagnosticsEnvelope<EPCISQueryResults>>> QueryEvents([FromBody] QueryEventsRequest request)
    {
        if (request == null)
        {
            return BadRequest("Request body is required.");
        }

        if (request.Options?.URL == null)
        {
            return BadRequest("Options.URL is required.");
        }

        request.Parameters ??= new EPCISQueryParameters();

        RewriteTestUrl(request.Options);

        var diagnosticsReport = new DiagnosticsReport();
        var client = _httpClientFactory.CreateClient("default");
        var results = await EPCISTraceabilityResolver.QueryEvents(request.Options, request.Parameters, client, diagnosticsReport);

        var envelope = new DiagnosticsEnvelope<EPCISQueryResults>
        {
            Data = results,
            Diagnostics = diagnosticsReport
        };

        var cacheId = _cache.Add(envelope);
        Response.Headers["X-Diagnostics-Id"] = cacheId;

        return Ok(envelope);
    }

    [HttpPost("epcis/query/traceback")]
    public async Task<ActionResult<DiagnosticsEnvelope<EPCISQueryResults>>> Traceback([FromBody] TracebackRequest request)
    {
        if (request == null || string.IsNullOrWhiteSpace(request.EPC))
        {
            return BadRequest("EPC is required.");
        }

        if (request.Options?.URL == null)
        {
            return BadRequest("Options.URL is required.");
        }

        if (!EPC.TryParse(request.EPC, out var epc, out var epcErr))
        {
            return BadRequest(epcErr);
        }

        RewriteTestUrl(request.Options);

        // Resolve the EPCIS Query Interface URL from the digital link URL in the request.
        var diagnosticsReport = new DiagnosticsReport();
        var url = await EPCISTraceabilityResolver.GetEPCISQueryInterfaceURL(new DigitalLinkQueryOptions()
        {
            URL = request.Options.URL,
        }, epc, _httpClientFactory.CreateClient("default"), diagnosticsReport);

        if (url != null)
        {
            var client = _httpClientFactory.CreateClient("default");
            var results = await EPCISTraceabilityResolver.Traceback(new EPCISQueryInterfaceOptions()
            {
                URL = url,
                EnableStackTrace = request.Options.EnableStackTrace,
                Version = request.Options.Version,
                Format = request.Options.Format
            }, epc, client, request.AdditionalParameters, diagnosticsReport);

            // Resolve master data if requested
            if (request.ResolveMasterData && results?.Document != null)
            {
                // Create DigitalLinkQueryOptions from EPCISQueryInterfaceOptions
                var digitalLinkOptions = new DigitalLinkQueryOptions
                {
                    URL = request.Options.URL,
                    Version = request.Options.Version,
                    Format = request.Options.Format
                };

                await OpenTraceability.Queries.MasterDataResolver.ResolveMasterData(
                    digitalLinkOptions,
                    results.Document,
                    client,
                    diagnosticsReport);
            }

            var envelope = new DiagnosticsEnvelope<EPCISQueryResults>
            {
                Data = results,
                Diagnostics = diagnosticsReport
            };

            var cacheId = _cache.Add(envelope);
            Response.Headers["X-Diagnostics-Id"] = cacheId;

            return Ok(envelope);
        }
        else
        {
            var envelope = new DiagnosticsEnvelope<EPCISQueryResults>
            {
                Data = null,
                Diagnostics = diagnosticsReport
            };
            var cacheId = _cache.Add(envelope);
            Response.Headers["X-Diagnostics-Id"] = cacheId;
            return Ok(envelope);
        }
    }

    private void RewriteTestUrl(DigitalLinkQueryOptions? options)
    {
        if (options?.URL == null)
        {
            return;
        }

        var replaced = ReplaceTestPlaceholder(options.URL.OriginalString);
        if (!string.IsNullOrEmpty(replaced) && Uri.TryCreate(replaced, UriKind.Absolute, out var newUri))
        {
            options.URL = newUri;
        }
    }

    private void RewriteTestUrl(EPCISQueryInterfaceOptions? options)
    {
        if (options?.URL == null)
        {
            return;
        }

        var replaced = ReplaceTestPlaceholder(options.URL.OriginalString);
        if (!string.IsNullOrEmpty(replaced) && Uri.TryCreate(replaced, UriKind.Absolute, out var newUri))
        {
            options.URL = newUri;
        }
    }

    private string? ReplaceTestPlaceholder(string? url)
    {
        const string placeholder = "{TEST_LOCAL_URL}";

        if (string.IsNullOrWhiteSpace(url) || !url.Contains(placeholder, StringComparison.OrdinalIgnoreCase))
        {
            return null;
        }

        var host = $"{Request.Scheme}://{Request.Host.Value}";
        return url.Replace(placeholder, host, StringComparison.OrdinalIgnoreCase);
    }
}


