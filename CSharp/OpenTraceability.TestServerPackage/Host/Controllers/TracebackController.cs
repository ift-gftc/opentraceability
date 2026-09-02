using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using OpenTraceability.TestServer.Core.Data;
using OpenTraceability.TestServer.Core.Models;
using OpenTraceability.TestServer.Infrastructure;
using OpenTraceability.TestServer.Services;

namespace OpenTraceability.TestServer.Controllers;

[ApiController]
[Authorize]
[Route("traceback")]
[Route("{datasetId}/traceback")]
[ServiceFilter(typeof(DatasetResolutionFilter))]
public class TracebackController : ControllerBase
{
    private readonly TracebackService _tracebackService;
    private readonly ITraceabilityStore _store;
    private readonly DatasetContext _dataset;

    public TracebackController(TracebackService tracebackService, ITraceabilityStore store, DatasetContext dataset)
    {
        _tracebackService = tracebackService;
        _store = store;
        _dataset = dataset;
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

        // default the dataset to the request's resolved dataset when not provided in the body;
        // a body-supplied dataset bypasses the resolution filter, so validate it here too
        if (string.IsNullOrWhiteSpace(request.DatasetId))
        {
            request.DatasetId = _dataset.DatasetId;
        }
        else if (request.DatasetId != _dataset.DatasetId && await _store.GetDatasetAsync(request.DatasetId!) == null)
        {
            return NotFound($"Unknown dataset '{request.DatasetId}'. Create it via PUT /datasets/{request.DatasetId} or a SeedData manifest.");
        }

        var result = await _tracebackService.ExecuteAsync(request);
        return Ok(result);
    }
}
