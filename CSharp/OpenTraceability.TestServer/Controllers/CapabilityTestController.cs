using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using OpenTraceability.TestServer.Services;

namespace OpenTraceability.TestServer.Controllers;

[ApiController]
[Authorize]
[Route("capability-test")]
public class CapabilityTestController : ControllerBase
{
    private readonly CapabilityTestClientService _capabilityTestService;

    public CapabilityTestController(CapabilityTestClientService capabilityTestService)
    {
        _capabilityTestService = capabilityTestService;
    }

    /// <summary>
    /// Runs the full GDST 2.0 capability test as a solution-provider client: start the test against
    /// the capability tool, fetch and store the generated data, advance the test, and poll the report
    /// to completion. Returns the final report.
    /// </summary>
    [HttpPost("run")]
    public async Task<IActionResult> Run([FromBody] CapabilityTestRequest request)
    {
        if (request == null || string.IsNullOrWhiteSpace(request.ToolUrl) || string.IsNullOrWhiteSpace(request.ToolApiKey))
        {
            return BadRequest("toolUrl and toolApiKey are required.");
        }

        try
        {
            var report = await _capabilityTestService.RunAsync(request);
            return Content(report.ToString(), "application/json");
        }
        catch (Exception ex)
        {
            return StatusCode(502, new { error = ex.Message });
        }
    }
}
