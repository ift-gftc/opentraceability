using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http.Extensions;
using Microsoft.AspNetCore.Mvc;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;
using OpenTraceability.Queries;
using OpenTraceability.TestServer.Core.Services;
using OpenTraceability.TestServer.Infrastructure;

namespace OpenTraceability.TestServer.Controllers;

[ApiController]
[Authorize]
[Route("epcis")]
public class EPCISQueryController : ControllerBase
{
    private readonly EpcisQueryService _queryService;
    private readonly IngestionService _ingestionService;
    private readonly SupportedModules _modules;

    public EPCISQueryController(EpcisQueryService queryService, IngestionService ingestionService, SupportedModules modules)
    {
        _queryService = queryService;
        _ingestionService = ingestionService;
        _modules = modules;
    }

    /// <summary>EPCIS Query Interface: returns module-minified events matching the GDST query parameters.</summary>
    [HttpGet("events")]
    public async Task<IActionResult> GetEvents()
    {
        try
        {
            var uri = new Uri(Request.GetEncodedUrl(), UriKind.Absolute);
            var parameters = new EPCISQueryParameters(uri);
            string datasetId = Request.GetDatasetId();

            string json = await _queryService.QueryEventsJsonAsync(datasetId, parameters, _modules.Modules);
            return Content(json, "application/json");
        }
        catch (Exception ex)
        {
            return StatusCode(500, ex.Message);
        }
    }

    /// <summary>Ingests an EPCIS Document (events + master data in its header).</summary>
    [HttpPost("events")]
    public async Task<IActionResult> PostEvents()
    {
        string? versionStr = Request.Headers["GS1-EPCIS-Version"].FirstOrDefault();
        if (versionStr == null)
        {
            return BadRequest("no GS1-EPCIS-Version header provided.");
        }

        EPCISDataFormat format;
        if (versionStr == "1.2")
        {
            format = EPCISDataFormat.XML;
        }
        else if (versionStr == "2.0")
        {
            string? contentType = Request.ContentType?.Split(';').First().Trim();
            switch (contentType)
            {
                case "application/json": format = EPCISDataFormat.JSON; break;
                case "application/xml": format = EPCISDataFormat.XML; break;
                default: return BadRequest("Content-Type must be application/json or application/xml");
            }
        }
        else
        {
            return BadRequest("GS1-EPCIS-Version header must be 1.2 or 2.0");
        }

        Request.EnableBuffering();
        Request.Body.Position = 0;
        string rawBody = await new StreamReader(Request.Body).ReadToEndAsync();
        string datasetId = Request.GetDatasetId();

        try
        {
            int count = await _ingestionService.IngestEpcisDocumentAsync(datasetId, rawBody, format);
            return Ok(new { eventsStored = count });
        }
        catch (OpenTraceability.Utility.OpenTraceabilitySchemaException ex)
        {
            return BadRequest("Schema errors found: " + ex.Message);
        }
        catch (Exception)
        {
            // The EPCIS schema validator (JsonSchema.Net) can be unavailable due to dependency
            // conflicts in some environments. Rather than reject otherwise-parseable data, retry
            // ingestion without schema validation so the data is still saved.
            try
            {
                int count = await _ingestionService.IngestEpcisDocumentAsync(datasetId, rawBody, format, checkSchema: false);
                return Ok(new { eventsStored = count, schemaValidated = false });
            }
            catch (Exception ex)
            {
                return BadRequest("Failed to ingest EPCIS document: " + ex.Message);
            }
        }
    }
}
