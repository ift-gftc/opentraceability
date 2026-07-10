using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace OpenTraceability.TestServer.Controllers;

[ApiController]
[AllowAnonymous]
[Route("health")]
public class HealthController : ControllerBase
{
    /// <summary>
    /// Lightweight liveness probe used by container orchestration and the docker build tests to
    /// confirm the server has started. Exempt from API key authentication so it is reachable even
    /// when keys are configured.
    /// </summary>
    [HttpGet]
    public IActionResult Get() => Ok(new { status = "Healthy" });
}
