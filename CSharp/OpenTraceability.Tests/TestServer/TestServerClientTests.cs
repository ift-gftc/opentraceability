using System.Net;
using Microsoft.Extensions.Hosting;
using OpenTraceability.Mappers;
using OpenTraceability.TestServer.Core.Client;
using OpenTraceability.TestServer.Core.Models;
using OpenTraceability.Utility;

namespace OpenTraceability.Tests.TestServer
{
    /// <summary>
    /// End-to-end tests of the typed TestServerClient against the real ASP.NET test server,
    /// exercising the same /{datasetId}/... routes a GDST capability tool would use.
    /// </summary>
    [TestFixture]
    public class TestServerClientTests
    {
        private const string ServerUrl = "https://localhost:4007";
        private const string ApiKey = "test";

        private IHost? _host;
        private HttpClient? _http;
        private TestServerClient? _client;

        [OneTimeSetUp]
        public void Setup()
        {
            var config = OpenTraceabilityTests.GetConfiguration("appsettings.TestServer");
            _host = OpenTraceability.TestServer.WebServiceFactory.Create(ServerUrl, config);

            // local dev certificate may not be trusted on every machine/CI agent
            var handler = new HttpClientHandler
            {
                ServerCertificateCustomValidationCallback = HttpClientHandler.DangerousAcceptAnyServerCertificateValidator
            };
            _http = new HttpClient(handler);
            _client = new TestServerClient(_http, ServerUrl, ApiKey);
        }

        [OneTimeTearDown]
        public void Teardown()
        {
            _client?.Dispose();
            _http?.Dispose();
            _host?.Dispose();
        }

        private static string LoadSampleDocument()
        {
            var loader = new EmbeddedResourceLoader();
            return loader.ReadString("OpenTraceability.Tests", "OpenTraceability.Tests.Data.gdst_data_withmasterdata.jsonld");
        }

        [Test]
        public async Task Health_ReturnsTrue()
        {
            Assert.That(await _client!.HealthAsync(), Is.True);
        }

        [Test]
        public async Task DatasetLifecycle_CreateListGetUpsertDelete()
        {
            string id = "client-test-lifecycle";
            await CleanupDataset(id);

            var created = await _client!.CreateDatasetAsync(new DatasetModel
            {
                DatasetId = id,
                Modules = new() { "Seafood" },
                Description = "lifecycle test"
            });
            Assert.That(created.DatasetId, Is.EqualTo(id));
            Assert.That(created.ExpandedModules, Does.Contain("Core").And.Contain("Seafood"));

            // duplicate create -> 409
            var conflict = Assert.ThrowsAsync<TestServerApiException>(() => _client.CreateDatasetAsync(new DatasetModel { DatasetId = id }));
            Assert.That(conflict!.StatusCode, Is.EqualTo(HttpStatusCode.Conflict));

            var listed = await _client.ListDatasetsAsync();
            Assert.That(listed.Select(d => d.DatasetId), Does.Contain(id));

            var upserted = await _client.UpsertDatasetAsync(id, new UpsertDatasetRequest { Modules = new() { "Wildcaught" } });
            Assert.That(upserted.Modules, Is.EquivalentTo(new[] { "Wildcaught" }));
            Assert.That(upserted.ExpandedModules, Does.Contain("Seafood"), "Wildcaught implies Seafood");

            await _client.DeleteDatasetAsync(id, purgeData: true);
            Assert.That(await _client.GetDatasetAsync(id), Is.Null);
        }

        [Test]
        public void ReservedOrInvalidDatasetIds_AreRejected()
        {
            var reserved = Assert.ThrowsAsync<TestServerApiException>(() =>
                _client!.CreateDatasetAsync(new DatasetModel { DatasetId = "epcis" }));
            Assert.That(reserved!.StatusCode, Is.EqualTo(HttpStatusCode.BadRequest));

            var badModules = Assert.ThrowsAsync<TestServerApiException>(() =>
                _client!.UpsertDatasetAsync("client-test-badmodules", new UpsertDatasetRequest { Modules = new() { "Wildcatch" } }));
            Assert.That(badModules!.StatusCode, Is.EqualTo(HttpStatusCode.BadRequest));
        }

        [Test]
        public async Task PerDatasetModules_DriveMinification_OnSameServer()
        {
            string doc = LoadSampleDocument();

            await CleanupDataset("client-test-core");
            await CleanupDataset("client-test-full");
            await _client!.UpsertDatasetAsync("client-test-core", new UpsertDatasetRequest());
            await _client.UpsertDatasetAsync("client-test-full", new UpsertDatasetRequest { Modules = new() { "Wildcaught", "Aquaculture" } });

            var core = _client.ForDataset("client-test-core");
            var full = _client.ForDataset("client-test-full");

            Assert.That(await core.PostEpcisDocumentAsync(doc, EPCISDataFormat.JSON), Is.GreaterThan(0));
            Assert.That(await full.PostEpcisDocumentAsync(doc, EPCISDataFormat.JSON), Is.GreaterThan(0));

            string coreJson = await core.QueryEventsRawAsync(new OpenTraceability.Queries.EPCISQueryParameters());
            Assert.That(coreJson, Does.Not.Contain("vesselCatchInformation"));
            Assert.That(coreJson, Does.Not.Contain("broodstockSource"));

            string fullJson = await full.QueryEventsRawAsync(new OpenTraceability.Queries.EPCISQueryParameters());
            Assert.That(fullJson, Does.Contain("vesselCatchInformation"));
            Assert.That(fullJson, Does.Contain("broodstockSource"));
        }

        [Test]
        public async Task PrefixedDigitalLinks_StayInsideTheDataset()
        {
            string id = "client-test-links";
            await CleanupDataset(id);
            await _client!.UpsertDatasetAsync(id, new UpsertDatasetRequest { Modules = new() { "Seafood" } });

            var ds = _client.ForDataset(id);
            Assert.That(ds.DigitalLinkUrl, Does.Contain($"/{id}/digitallink/"));

            var links = await ds.GetProductLinksAsync("09506000134376");
            Assert.That(links, Is.Not.Empty);
            Assert.That(links.Select(l => l.link), Has.Some.Contains($"/{id}/epcis"),
                "links must carry the dataset prefix so a capability tool stays scoped to the dataset");
        }

        [Test]
        public void UnknownDataset_Returns404_ForReadsAndWrites()
        {
            var read = Assert.ThrowsAsync<TestServerApiException>(() =>
                _client!.ForDataset("client-test-unknown").QueryEventsRawAsync(new OpenTraceability.Queries.EPCISQueryParameters()));
            Assert.That(read!.StatusCode, Is.EqualTo(HttpStatusCode.NotFound));

            var write = Assert.ThrowsAsync<TestServerApiException>(() =>
                _client!.ForDataset("client-test-unknown").PostEpcisDocumentAsync(LoadSampleDocument(), EPCISDataFormat.JSON));
            Assert.That(write!.StatusCode, Is.EqualTo(HttpStatusCode.NotFound));
        }

        [Test]
        public async Task ClearDataset_PurgesData()
        {
            string id = "client-test-clear";
            await CleanupDataset(id);
            await _client!.UpsertDatasetAsync(id, new UpsertDatasetRequest { Modules = new() { "Wildcaught" } });

            var ds = _client.ForDataset(id);
            Assert.That(await ds.PostEpcisDocumentAsync(LoadSampleDocument(), EPCISDataFormat.JSON), Is.GreaterThan(0));

            await _client.ClearDatasetAsync(id);

            string json = await ds.QueryEventsRawAsync(new OpenTraceability.Queries.EPCISQueryParameters());
            Assert.That(json, Does.Not.Contain("ObjectEvent"), "events should be purged");
        }

        private async Task CleanupDataset(string id)
        {
            // tests share a persistent epcis.db file; start each scenario from a clean slate
            if (await _client!.GetDatasetAsync(id) != null)
            {
                await _client.DeleteDatasetAsync(id, purgeData: true);
            }
        }
    }
}
