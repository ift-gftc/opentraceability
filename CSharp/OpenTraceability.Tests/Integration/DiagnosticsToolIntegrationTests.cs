using System.Net.Http.Json;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.Extensions.Configuration;
using NUnit.Framework;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;
using OpenTraceability.Queries;
using DiagnosticsTool.Models.Requests;
using Newtonsoft.Json.Linq;
using Microsoft.AspNetCore.TestHost;

namespace OpenTraceability.Tests.Integration;

[TestFixture]
[Category("Integration")]
public class DiagnosticsToolIntegrationTests
{
    private static IWebHost? _epcisTestServer;
    private static IWebHost? _diagnosticsTool;
    private static IConfiguration? _config;

    [OneTimeSetUp]
    public void OneTimeSetup()
    {
        _config = OpenTraceabilityTests.GetConfiguration("appsettings.TestServer");
        _epcisTestServer = OpenTraceability.TestServer.WebServiceFactory.Create("https://localhost:4001", _config);
        _diagnosticsTool = DiagnosticsTool.WebServiceFactory.Create("https://localhost:4002", _config);
    }

    [OneTimeTearDown]
    public void OneTimeTearDown()
    {
        _diagnosticsTool?.Dispose();
        _epcisTestServer?.Dispose();
    }

    [Test]
    [TestCase("aggregation_event_all_possible_fields.jsonld")]
    public async Task QueryEvents_EndToEnd(string filename)
    {
        var epcisClient = new EPCISTestServerClient("https://localhost:4001", OpenTraceability.Mappers.EPCISDataFormat.JSON, EPCISVersion.V2);
        string data = OpenTraceabilityTests.ReadTestData(filename);
        var sourceDoc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(data);
        string blobId = await epcisClient.Post(sourceDoc);

        using HttpClient client = new HttpClient();
        client.BaseAddress = new Uri("https://localhost:4002/");

        foreach (var evt in sourceDoc.Events)
        {
            foreach (var prod in evt.Products)
            {
                var options = new EPCISQueryInterfaceOptions
                {
                    URL = new Uri($"https://localhost:4001/epcis/{blobId}"), // remove trailing slash so controller builds correct URL
                    Version = EPCISVersion.V2,
                    Format = OpenTraceability.Mappers.EPCISDataFormat.JSON
                };
                var parameters = new EPCISQueryParameters(prod.EPC);
                var request = new QueryEventsRequest { Options = options, Parameters = parameters };

                var response = await client.PostAsJsonAsync("/api/v1/diagnostics/epcis/query/events", request);
                Assert.That(response.IsSuccessStatusCode, Is.True, $"DiagnosticsTool returned HTTP {(int)response.StatusCode}");

                string json = await response.Content.ReadAsStringAsync();
                var j = JObject.Parse(json);
                var eventsToken = j.SelectToken("Data.Document.Events") ?? j.SelectToken("data.document.events");
                Assert.That(eventsToken, Is.Not.Null, "Events token missing");
                Assert.That(eventsToken!.Type, Is.EqualTo(JTokenType.Array));
                Assert.That(eventsToken!.Count(), Is.GreaterThan(0), "No events returned");
                var requestsToken = j.SelectToken("Diagnostics.Requests") ?? j.SelectToken("diagnostics.requests");
                Assert.That(requestsToken, Is.Not.Null, "Diagnostics requests missing");
                Assert.That(requestsToken!.Count(), Is.GreaterThan(0), "No diagnostics requests captured");
            }
        }
    }

    [Test]
    [TestCase("traceback_tests.jsonld")]
    public async Task Traceback_EndToEnd(string filename)
    {
        var epcisClient = new EPCISTestServerClient("https://localhost:4001", OpenTraceability.Mappers.EPCISDataFormat.JSON, EPCISVersion.V2);
        string data = OpenTraceabilityTests.ReadTestData(filename);
        var sourceDoc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(data);
        string blobId = await epcisClient.Post(sourceDoc);

        using HttpClient client = new HttpClient();
        client.BaseAddress = new Uri("https://localhost:4002/");

        var firstEpc = sourceDoc.Events.SelectMany(e => e.Products).Select(p => p.EPC).First();

        var options = new EPCISQueryInterfaceOptions
        {
            URL = new Uri($"https://localhost:4001/epcis/{blobId}"),
            Version = EPCISVersion.V2,
            Format = OpenTraceability.Mappers.EPCISDataFormat.JSON
        };

        var request = new TracebackRequest { Options = options, EPC = firstEpc.ToString() };

        var response = await client.PostAsJsonAsync("/api/v1/diagnostics/epcis/query/traceback", request);
        Assert.That(response.IsSuccessStatusCode, Is.True, $"DiagnosticsTool returned HTTP {(int)response.StatusCode}");

        string json = await response.Content.ReadAsStringAsync();
        var j = JObject.Parse(json);
        var eventsToken = j.SelectToken("Data.Document.Events") ?? j.SelectToken("data.document.events");
        Assert.That(eventsToken, Is.Not.Null, "Events token missing");
        Assert.That(eventsToken!.Type, Is.EqualTo(JTokenType.Array));
        Assert.That(eventsToken!.Count(), Is.GreaterThan(0), "No events returned");
        var requestsToken = j.SelectToken("Diagnostics.Requests") ?? j.SelectToken("diagnostics.requests");
        Assert.That(requestsToken, Is.Not.Null, "Diagnostics requests missing");
        Assert.That(requestsToken!.Count(), Is.GreaterThan(0), "No diagnostics requests captured");
    }

    [Test]
    [TestCase("beef-leather-example", "https://id.gs1.org/00/106141412345678908")]
    public async Task Traceback_Default_Data(string blobId, string epc)
    {
        using HttpClient client = new HttpClient();
        client.BaseAddress = new Uri("https://localhost:4002/");

        var options = new EPCISQueryInterfaceOptions
        {
            URL = new Uri($"https://localhost:4001/epcis/{blobId}"),
            Version = EPCISVersion.V2,
            Format = OpenTraceability.Mappers.EPCISDataFormat.JSON
        };

        var request = new TracebackRequest { Options = options, EPC = epc };

        var response = await client.PostAsJsonAsync("/api/v1/diagnostics/epcis/query/traceback", request);
        Assert.That(response.IsSuccessStatusCode, Is.True, $"DiagnosticsTool returned HTTP {(int)response.StatusCode}");

        string json = await response.Content.ReadAsStringAsync();
        var j = JObject.Parse(json);
        var eventsToken = j.SelectToken("Data.Document.Events") ?? j.SelectToken("data.document.events");
        Assert.That(eventsToken, Is.Not.Null, "Events token missing");
        Assert.That(eventsToken!.Type, Is.EqualTo(JTokenType.Array));
        Assert.That(eventsToken!.Count(), Is.GreaterThan(0), "No events returned");
        var requestsToken = j.SelectToken("Diagnostics.Requests") ?? j.SelectToken("diagnostics.requests");
        Assert.That(requestsToken, Is.Not.Null, "Diagnostics requests missing");
        Assert.That(requestsToken!.Count(), Is.GreaterThan(0), "No diagnostics requests captured");
    }
}
