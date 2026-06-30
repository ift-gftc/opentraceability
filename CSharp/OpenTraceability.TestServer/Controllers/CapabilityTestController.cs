using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using OpenTraceability.TestServer.Core.Data;
using OpenTraceability.TestServer.Core.Models;
using OpenTraceability.TestServer.Infrastructure;
using OpenTraceability.TestServer.Services;

namespace OpenTraceability.TestServer.Controllers;

[ApiController]
[Authorize]
[Route("capability-test")]
[Route("{datasetId}/capability-test")]
public class CapabilityTestController : ControllerBase
{
    private readonly CapabilityTestClientService _capabilityTestService;
    private readonly ITraceabilityStore _store;

    public CapabilityTestController(CapabilityTestClientService capabilityTestService, ITraceabilityStore store)
    {
        _capabilityTestService = capabilityTestService;
        _store = store;
    }

    /// <summary>
    /// Runs the full GDST 2.0 capability test as a solution-provider client: start the test against
    /// the capability tool, fetch and store the generated data, advance the test, and poll the report
    /// to completion. Returns the final report. The test runs against the request's dataset — its
    /// persisted modules are sent to the tool, and the resolver URL handed to the tool embeds the
    /// dataset path so every tool request is served (and minified) from that dataset.
    /// </summary>
    [HttpPost("run")]
    public async Task<IActionResult> Run([FromBody] CapabilityTestRequest request)
    {
        if (request == null || string.IsNullOrWhiteSpace(request.ToolUrl) || string.IsNullOrWhiteSpace(request.ToolApiKey))
        {
            return BadRequest("toolUrl and toolApiKey are required.");
        }

        // body DatasetId wins; fall back to the route segment / X-Dataset-Id header / "default"
        string datasetId = string.IsNullOrWhiteSpace(request.DatasetId) ? Request.GetDatasetId() : request.DatasetId;
        request.DatasetId = datasetId;

        // The dataset's persisted module set is the whole point of the test — never auto-create.
        var dataset = await _store.GetDatasetAsync(datasetId);
        if (dataset == null)
        {
            return NotFound($"Unknown dataset '{datasetId}'. Create it via PUT /datasets/{datasetId} or a SeedData manifest.");
        }

        try
        {
            var report = await _capabilityTestService.RunAsync(request, dataset);
            return Content(report.ToString(), "application/json");
        }
        catch (Exception ex)
        {
            return StatusCode(502, new { error = ex.Message });
        }
    }
}
