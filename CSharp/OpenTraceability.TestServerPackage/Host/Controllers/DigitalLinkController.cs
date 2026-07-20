using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Newtonsoft.Json;
using OpenTraceability;
using OpenTraceability.TestServer.Core.Services;
using OpenTraceability.TestServer.Infrastructure;

namespace OpenTraceability.TestServer.Controllers;

[ApiController]
[Authorize]
[Route("digitallink")]
[Route("{datasetId}/digitallink")]
[ServiceFilter(typeof(DatasetResolutionFilter))]
public class DigitalLinkController : ControllerBase
{
    // The datasetId passed to the link builder is the ROUTE segment only (not the resolved
    // dataset): a dataset-prefixed request emits dataset-prefixed links so a capability tool
    // started at /{datasetId}/digitallink/ stays inside the dataset, while bare-route requests
    // (header-scoped consumers) keep getting unprefixed links.
    private readonly DigitalLinkService _digitalLink;
    private readonly IConfiguration _config;

    public DigitalLinkController(DigitalLinkService digitalLink, IConfiguration config)
    {
        _digitalLink = digitalLink;
        _config = config;
    }

    private string BaseUrl => Request.GetBaseUrl(_config);

    [HttpGet("01/{gtin}")]
    [HttpGet("gtin/{gtin}")]
    public IActionResult GetProduct(string gtin, [FromQuery] string? linkType, string? datasetId)
        => Respond($"product/{gtin}", linkType, datasetId);

    [HttpGet("01/{gtin}/10/{lot}")]
    [HttpGet("gtin/{gtin}/lot/{lot}")]
    public IActionResult GetEpcClass(string gtin, string lot, [FromQuery] string? linkType, string? datasetId)
        => Respond($"product/{gtin}", linkType, datasetId);

    [HttpGet("01/{gtin}/21/{serial}")]
    [HttpGet("gtin/{gtin}/serial/{serial}")]
    public IActionResult GetEpcInstance(string gtin, string serial, [FromQuery] string? linkType, string? datasetId)
        => Respond($"product/{gtin}", linkType, datasetId);

    [HttpGet("00/{sscc}")]
    [HttpGet("sscc/{sscc}")]
    public IActionResult GetSSCC(string sscc, [FromQuery] string? linkType, string? datasetId)
        => Respond(null, linkType, datasetId);

    [HttpGet("414/{gln}")]
    [HttpGet("gln/{gln}")]
    public IActionResult GetLocation(string gln, [FromQuery] string? linkType, string? datasetId)
        => Respond($"location/{gln}", linkType, datasetId);

    [HttpGet("417/{pgln}")]
    [HttpGet("pgln/{pgln}")]
    public IActionResult GetParty(string pgln, [FromQuery] string? linkType, string? datasetId)
        => Respond($"party/{pgln}", linkType, datasetId);

    /// <summary>
    /// Produces the digital link response for an identifier, negotiating the shape from the request:
    /// a linkset when the client asks for one, a redirect for a browser-like client, and otherwise
    /// the legacy flat digital link array (so 1.1.2 consumers and existing tests keep working).
    /// </summary>
    /// <remarks>
    /// The three modes, applied in order, mirror the GS1-Conformant Resolver standard:
    /// (1) <c>Accept: application/linkset+json</c> or <c>?linkType=linkset</c> returns the linkset and
    /// SHALL NOT redirect; (2) <c>Accept: text/html</c> redirects (302) to the requested link type or
    /// the default link, returning 404 when the requested type is unavailable, and forwards the whole
    /// query string; (3) anything else returns the legacy JSON array filtered by <c>?linkType</c>.
    /// </remarks>
    /// <param name="masterDataPath">The relative master data path for the identifier, or null when it has none (e.g. SSCC).</param>
    /// <param name="linkType">The requested link type from the query string, if any.</param>
    /// <param name="datasetId">The route dataset segment, forwarded so emitted links stay dataset-scoped.</param>
    private IActionResult Respond(string? masterDataPath, string? linkType, string? datasetId)
    {
        if (WantsLinkset(linkType))
        {
            string anchor = $"{Request.Scheme}://{Request.Host}{Request.Path}";
            var linkset = _digitalLink.BuildLinkset(BaseUrl, anchor, masterDataPath, datasetId);
            // Serialize with Newtonsoft so the linkset's dynamic URI keys (JsonExtensionData) render.
            return Content(JsonConvert.SerializeObject(linkset), DigitalLinkVocab.LinksetMediaType);
        }

        if (WantsRedirect())
        {
            string? target = _digitalLink.ResolveTargetHref(BaseUrl, masterDataPath, linkType, datasetId);
            if (target == null)
            {
                return NotFound();
            }

            // Forward the entirety of the incoming query string to the target, per the standard.
            return Redirect(target + Request.QueryString.Value);
        }

        return Ok(_digitalLink.BuildLinks(BaseUrl, masterDataPath, linkType, datasetId));
    }

    /// <summary>Returns true when the client requested the linkset (by media type or linkType value).</summary>
    private bool WantsLinkset(string? linkType)
    {
        if (!string.IsNullOrWhiteSpace(linkType) && linkType!.ToLower() == DigitalLinkVocab.LinksetLinkType)
        {
            return true;
        }

        return AcceptContains(DigitalLinkVocab.LinksetMediaType);
    }

    /// <summary>Returns true when the client is browser-like (accepts HTML) and should be redirected.</summary>
    private bool WantsRedirect() => AcceptContains("text/html");

    /// <summary>Returns true when the request's Accept header contains the given media type.</summary>
    private bool AcceptContains(string mediaType)
    {
        foreach (var accept in Request.GetTypedHeaders().Accept)
        {
            if (string.Equals(accept.MediaType.Value, mediaType, System.StringComparison.OrdinalIgnoreCase))
            {
                return true;
            }
        }

        return false;
    }
}
