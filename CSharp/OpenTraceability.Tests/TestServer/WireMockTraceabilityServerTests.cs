using System.Net;
using System.Net.Http;
using System.Threading.Tasks;
using OpenTraceability.GDST;
using OpenTraceability.GDST.Modules;
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
        public async Task MultiDataset_PrefixedRoutes_ServeDifferentModuleTiers()
        {
            string doc = LoadSampleDocument();
            using var server = WireMockTraceabilityServer.StartWireMockTraceabilityServer(new WireMockTraceabilityConfig
            {
                Modules = new() { GdstModule.Wildcaught, GdstModule.Aquaculture },
                Datasets = new()
                {
                    new WireMockDataset { DatasetId = "core-tier", Modules = new(), SeedEpcisDocuments = new() { doc } },
                    new WireMockDataset { DatasetId = "full-tier", Modules = new() { GdstModule.Wildcaught, GdstModule.Aquaculture }, SeedEpcisDocuments = new() { doc } }
                }
            });

            using var client = new HttpClient();

            var coreResp = await client.GetAsync($"{server.Url}/core-tier/epcis/events");
            string coreBody = await coreResp.Content.ReadAsStringAsync();
            Assert.That(coreResp.StatusCode, Is.EqualTo(HttpStatusCode.OK), coreBody);
            Assert.That(coreBody, Does.Not.Contain("vesselCatchInformation"));
            Assert.That(coreBody, Does.Not.Contain("broodstockSource"));

            var fullResp = await client.GetAsync($"{server.Url}/full-tier/epcis/events");
            string fullBody = await fullResp.Content.ReadAsStringAsync();
            Assert.That(fullResp.StatusCode, Is.EqualTo(HttpStatusCode.OK), fullBody);
            Assert.That(fullBody, Does.Contain("vesselCatchInformation"));
            Assert.That(fullBody, Does.Contain("broodstockSource"));
        }

        [Test]
        public async Task UnknownDataset_Returns404()
        {
            using var server = WireMockTraceabilityServer.StartWireMockTraceabilityServer(new WireMockTraceabilityConfig
            {
                Modules = new() { GdstModule.Wildcaught }
            });

            using var client = new HttpClient();

            var response = await client.GetAsync($"{server.Url}/nope/epcis/events");
            Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.NotFound));

            var dlResponse = await client.GetAsync($"{server.Url}/nope/digitallink/01/09506000134376");
            Assert.That(dlResponse.StatusCode, Is.EqualTo(HttpStatusCode.NotFound));
        }

        [Test]
        public async Task PrefixedDigitalLink_EmitsDatasetPrefixedLinks()
        {
            using var server = WireMockTraceabilityServer.StartWireMockTraceabilityServer(new WireMockTraceabilityConfig
            {
                Datasets = new()
                {
                    new WireMockDataset { DatasetId = "ds1", Modules = new() { GdstModule.Seafood } }
                }
            });

            using var client = new HttpClient();

            var response = await client.GetAsync($"{server.Url}/ds1/digitallink/01/09506000134376");
            string body = await response.Content.ReadAsStringAsync();

            Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.OK), body);
            Assert.That(body, Does.Contain("/ds1/epcis"), "prefixed requests must emit prefixed links so consumers stay in the dataset");
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
