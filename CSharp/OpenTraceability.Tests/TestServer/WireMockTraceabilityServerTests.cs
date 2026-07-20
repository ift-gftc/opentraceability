using System.Net;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Threading.Tasks;
using Newtonsoft.Json;
using OpenTraceability;
using OpenTraceability.GDST;
using OpenTraceability.GDST.Modules;
using OpenTraceability.Models.MasterData;
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

        [Test]
        public async Task DigitalLink_LinksetAcceptHeader_ReturnsLinkset()
        {
            using var server = WireMockTraceabilityServer.StartWireMockTraceabilityServer(new WireMockTraceabilityConfig
            {
                Modules = new() { GdstModule.Wildcaught, GdstModule.Aquaculture }
            });

            using var client = new HttpClient();
            client.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue(DigitalLinkVocab.LinksetMediaType));

            var response = await client.GetAsync($"{server.Url}/digitallink/01/09506000134376");
            string body = await response.Content.ReadAsStringAsync();

            Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.OK), body);
            Assert.That(response.Content.Headers.ContentType?.MediaType, Is.EqualTo(DigitalLinkVocab.LinksetMediaType));

            // The body must round-trip through the core linkset model and expose the expected link types.
            Linkset? linkset = JsonConvert.DeserializeObject<Linkset>(body);
            Assert.That(linkset, Is.Not.Null);
            Assert.That(linkset!.linkset, Has.Count.EqualTo(1));

            LinksetItem item = linkset.linkset[0];
            Assert.That(item.anchor, Does.Contain("/digitallink/01/09506000134376"));
            Assert.That(item.GetLinks(DigitalLinkVocab.EpcisUri), Is.Not.Empty, "linkset must expose an epcis link");
            Assert.That(item.GetLinks(DigitalLinkVocab.MasterDataUri), Is.Not.Empty, "product linkset must expose a master data link");
            Assert.That(item.GetLinks(DigitalLinkVocab.DefaultLinkUri), Is.Not.Empty, "linkset must expose exactly one default link");
        }

        [Test]
        public async Task DigitalLink_LinkTypeLinksetParam_ReturnsLinkset()
        {
            using var server = WireMockTraceabilityServer.StartWireMockTraceabilityServer(new WireMockTraceabilityConfig
            {
                Modules = new() { GdstModule.Wildcaught }
            });

            using var client = new HttpClient();

            var response = await client.GetAsync($"{server.Url}/digitallink/01/09506000134376?linkType=linkset");
            string body = await response.Content.ReadAsStringAsync();

            Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.OK), body);
            Assert.That(body, Does.Contain(DigitalLinkVocab.EpcisUri));
        }

        [Test]
        public async Task DigitalLink_BrowserAccept_RedirectsToDefaultLink()
        {
            using var server = WireMockTraceabilityServer.StartWireMockTraceabilityServer(new WireMockTraceabilityConfig
            {
                Modules = new() { GdstModule.Wildcaught }
            });

            using var handler = new HttpClientHandler { AllowAutoRedirect = false };
            using var client = new HttpClient(handler);
            client.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("text/html"));

            var response = await client.GetAsync($"{server.Url}/digitallink/01/09506000134376");

            // A product's default link is its master data page.
            Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.Found));
            Assert.That(response.Headers.Location?.ToString(), Does.Contain("/masterdata/product/09506000134376"));
        }

        [Test]
        public async Task DigitalLink_BrowserAccept_SpecificLinkType_RedirectsToThatLink()
        {
            using var server = WireMockTraceabilityServer.StartWireMockTraceabilityServer(new WireMockTraceabilityConfig
            {
                Modules = new() { GdstModule.Wildcaught }
            });

            using var handler = new HttpClientHandler { AllowAutoRedirect = false };
            using var client = new HttpClient(handler);
            client.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("text/html"));

            var response = await client.GetAsync($"{server.Url}/digitallink/01/09506000134376?linkType={DigitalLinkVocab.EpcisCurie}");

            Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.Found));
            Assert.That(response.Headers.Location?.ToString(), Does.Contain("/epcis"));
        }

        [Test]
        public async Task DigitalLink_BrowserAccept_ForwardsQueryString()
        {
            using var server = WireMockTraceabilityServer.StartWireMockTraceabilityServer(new WireMockTraceabilityConfig
            {
                Modules = new() { GdstModule.Wildcaught }
            });

            using var handler = new HttpClientHandler { AllowAutoRedirect = false };
            using var client = new HttpClient(handler);
            client.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("text/html"));

            var response = await client.GetAsync($"{server.Url}/digitallink/01/09506000134376?lot=ABC123");

            Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.Found));
            Assert.That(response.Headers.Location?.ToString(), Does.Contain("lot=ABC123"), "the resolver SHALL forward the whole query string on redirect");
        }

        [Test]
        public async Task DigitalLink_BrowserAccept_UnavailableLinkType_Returns404()
        {
            using var server = WireMockTraceabilityServer.StartWireMockTraceabilityServer(new WireMockTraceabilityConfig
            {
                Modules = new() { GdstModule.Wildcaught }
            });

            using var handler = new HttpClientHandler { AllowAutoRedirect = false };
            using var client = new HttpClient(handler);
            client.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("text/html"));

            // An SSCC has no master data link, so requesting one SHALL 404.
            var response = await client.GetAsync($"{server.Url}/digitallink/00/106141412345678908?linkType={DigitalLinkVocab.MasterDataCurie}");

            Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.NotFound));
        }
    }
}
