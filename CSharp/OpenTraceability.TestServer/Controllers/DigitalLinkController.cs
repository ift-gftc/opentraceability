using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using OpenTraceability.TestServer.Core.Services;
using OpenTraceability.TestServer.Infrastructure;

namespace OpenTraceability.TestServer.Controllers;

[ApiController]
[Authorize]
[Route("digitallink")]
public class DigitalLinkController : ControllerBase
{
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
    public IActionResult GetProduct(string gtin, [FromQuery] string? linkType)
        => Ok(_digitalLink.ForProduct(BaseUrl, gtin, linkType));

    [HttpGet("01/{gtin}/10/{lot}")]
    [HttpGet("gtin/{gtin}/lot/{lot}")]
    public IActionResult GetEpcClass(string gtin, string lot, [FromQuery] string? linkType)
        => Ok(_digitalLink.ForEpcClass(BaseUrl, gtin, lot, linkType));

    [HttpGet("01/{gtin}/21/{serial}")]
    [HttpGet("gtin/{gtin}/serial/{serial}")]
    public IActionResult GetEpcInstance(string gtin, string serial, [FromQuery] string? linkType)
        => Ok(_digitalLink.ForEpcInstance(BaseUrl, gtin, serial, linkType));

    [HttpGet("00/{sscc}")]
    [HttpGet("sscc/{sscc}")]
    public IActionResult GetSSCC(string sscc, [FromQuery] string? linkType)
        => Ok(_digitalLink.ForSSCC(BaseUrl, sscc, linkType));

    [HttpGet("414/{gln}")]
    [HttpGet("gln/{gln}")]
    public IActionResult GetLocation(string gln, [FromQuery] string? linkType)
        => Ok(_digitalLink.ForLocation(BaseUrl, gln, linkType));

    [HttpGet("417/{pgln}")]
    [HttpGet("pgln/{pgln}")]
    public IActionResult GetParty(string pgln, [FromQuery] string? linkType)
        => Ok(_digitalLink.ForParty(BaseUrl, pgln, linkType));
}
