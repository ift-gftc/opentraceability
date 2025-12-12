using Microsoft.AspNetCore.Mvc;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.MasterData;
using System;

namespace OpenTraceability.TestServer.Controllers
{
    [Route("example-data")]
    [ApiController]
    public class ExampleDataController : ControllerBase
    {
        private readonly IConfiguration _config;
        public ExampleDataController(IConfiguration config)
        {
            _config = config;
        }

        [HttpGet]
        [Route("417/{pgln}")]
        public async Task<IActionResult> GetLinks(string pgln)
        {
            string? baseURL = _config["BaseURL"];
            if (string.IsNullOrEmpty(baseURL))
            {
                return BadRequest("Base Url is not configured.");
            }
            string documentIdentifier = pgln.Split(":").LastOrDefault();
            if (string.IsNullOrEmpty(documentIdentifier))
            {
                return BadRequest("Invalid pgln.");
            }

            string eventURL = $"{baseURL}/example-data/{documentIdentifier}";

            List<DigitalLink> links = new()
            {
                new DigitalLink()
                {
                    link = eventURL,
                    linkType = "gs1:epcis"
                }
            };
            return Ok(links);
        }

        [HttpGet]
        [Route("{*path}")]
        public async Task<IActionResult> Get(string path)
        {
            if (string.IsNullOrWhiteSpace(path))
            {
                return BadRequest();
            }

            string executingDirectory = AppContext.BaseDirectory;

            // Get the assembly containing the embedded resources
            var assembly = typeof(ExampleDataController).Assembly;

            // Find the embedded resource that ends with {id}.epcis.jsonld
            string[] pathParts = path.Split("/example-data/");
            string[] subParts = pathParts[0].Split("/");
            string documentName = subParts[0];

            string targetResourceSuffix = $"{documentName}.xml";
            var resources = assembly.GetManifestResourceNames();
            string? resourceName = resources.FirstOrDefault(r => r.EndsWith(targetResourceSuffix, StringComparison.OrdinalIgnoreCase));

            if (resourceName == null)
            {
                return NotFound();
            }

            // Load the embedded resource
            using var stream = assembly.GetManifestResourceStream(resourceName);
            if (stream == null)
            {
                return NotFound();
            }

            using var reader = new StreamReader(stream);
            string rawData = reader.ReadToEnd();
            if (string.IsNullOrEmpty(rawData))
            {
                return NotFound($"Could not find example data for document name {documentName}");
            }

            EPCISQueryDocument doc = OpenTraceabilityMappers.EPCISQueryDocument.XML.Map(rawData);
            string json = OpenTraceabilityMappers.EPCISQueryDocument.JSON.Map(doc);

            // slight, jittered delay to simulate actual activity
            int delayMs = Random.Shared.Next(500, 5000);
            await Task.Delay(delayMs);

            // write the response and set the header appropriately
            this.HttpContext.Response.Headers.TryAdd("Content-Type", "application/json");
            await this.HttpContext.Response.WriteAsync(json);
            return Empty;
        }
    }
}
