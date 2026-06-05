using System.Net;
using System.Net.Http;
using System.Threading.Tasks;
using OpenTraceability.TestServer.Core.Modules;
using OpenTraceability.TestServer.Core.WireMock;
using OpenTraceability.Utility;

namespace OpenTraceability.Tests.TestServer
{
    public class WireMockTraceabilityServerTests
    {
        private static string LoadSampleDocument()
        {
            var loader = new EmbeddedResourceLoader();
            return loader.ReadString("OpenTraceability.Tests", "OpenTraceability.Tests.Data.gdst_data_withmasterdata.jsonld");
        }

        [Test]
        public async Task EpcisQuery_CoreOnly_OmitsNonCoreKeys()
        {
            string doc = LoadSampleDocument();
            using var server = WireMockTraceabilityServer.StartWireMockTraceabilityServer(new WireMockTraceabilityConfig
            {
                Modules = new() { GdstModule.Core },
                SeedEpcisDocuments = new() { doc }
            });

            using var client = new HttpClient();

            var response = await client.GetAsync($"{server.Url}/epcis/events");
            string body = await response.Content.ReadAsStringAsync();

            Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.OK), body);
            Assert.That(body, Does.Contain("eventID").IgnoreCase.Or.Contain("bizStep"));
            Assert.That(body, Does.Not.Contain("broodstockSource"));
            Assert.That(body, Does.Not.Contain("vesselCatchInformation"));
            Assert.That(body, Does.Not.Contain("unloadingPort"));
        }

        [Test]
        public async Task EpcisQuery_AllModules_IncludesGdstKeys()
        {
            string doc = LoadSampleDocument();
            using var server = WireMockTraceabilityServer.StartWireMockTraceabilityServer(new WireMockTraceabilityConfig
            {
                Modules = new() { GdstModule.Wildcaught, GdstModule.Aquaculture },
                SeedEpcisDocuments = new() { doc }
            });

            using var client = new HttpClient();

            var response = await client.GetAsync($"{server.Url}/epcis/events");
            string body = await response.Content.ReadAsStringAsync();

            Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.OK), body);
            Assert.That(body, Does.Contain("broodstockSource"));
            Assert.That(body, Does.Contain("vesselCatchInformation"));
        }

        [Test]
        public async Task DigitalLink_ReturnsEpcisAndMasterDataLinks()
        {
            using var server = WireMockTraceabilityServer.StartWireMockTraceabilityServer(new WireMockTraceabilityConfig
            {
                Modules = new() { GdstModule.Wildcaught, GdstModule.Aquaculture }
            });

            using var client = new HttpClient();

            var response = await client.GetAsync($"{server.Url}/digitallink/01/09506000134376");
            string body = await response.Content.ReadAsStringAsync();

            Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.OK), body);
            Assert.That(body, Does.Contain("gs1:epcis"));
            Assert.That(body, Does.Contain("gs1:masterData"));
        }

        [Test]
        public async Task MasterData_ReturnsSeededElement()
        {
            string doc = LoadSampleDocument();
            using var server = WireMockTraceabilityServer.StartWireMockTraceabilityServer(new WireMockTraceabilityConfig
            {
                Modules = new() { GdstModule.Wildcaught, GdstModule.Aquaculture },
                SeedEpcisDocuments = new() { doc }
            });

            // a trade item id present in the seeded sample document
            string id = System.Uri.EscapeDataString("urn:gdst:example.org:product:class:fisherman01.tunau");
            using var client = new HttpClient();
            var response = await client.GetAsync($"{server.Url}/masterdata/product/{id}");
            string body = await response.Content.ReadAsStringAsync();

            Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.OK), body);
            Assert.That(body, Does.Contain("@context"));
        }
    }
}
