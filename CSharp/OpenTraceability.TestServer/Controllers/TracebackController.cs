using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using OpenTraceability.TestServer.Infrastructure;
using OpenTraceability.TestServer.Services;

namespace OpenTraceability.TestServer.Controllers;

[ApiController]
[Authorize]
[Route("traceback")]
public class TracebackController : ControllerBase
{
    private readonly TracebackService _tracebackService;

    public TracebackController(TracebackService tracebackService)
    {
        _tracebackService = tracebackService;
    }

    /// <summary>
    /// Executes an EPCIS traceback against an external server for the given EPCs and stores the
    /// retrieved events and master data in this server's database.
    /// </summary>
    [HttpPost]
    public async Task<IActionResult> Run([FromBody] TracebackRequest request)
    {
        if (request == null || request.Epcs.Count == 0 || string.IsNullOrWhiteSpace(request.ResolverUrl))
        {
            return BadRequest("epcs and resolverUrl are required.");
        }

        // default the dataset to the request header when not provided in the body
        if (string.IsNullOrWhiteSpace(request.DatasetId))
        {
            request.DatasetId = Request.GetDatasetId();
        }

        var result = await _tracebackService.ExecuteAsync(request);
        return Ok(result);
    }
}
