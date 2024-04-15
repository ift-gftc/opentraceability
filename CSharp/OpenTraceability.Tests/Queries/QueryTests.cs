using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Configuration;
using OpenTraceability.GDST.MasterData;
using OpenTraceability.Mappers;
using OpenTraceability.Queries;

namespace OpenTraceability.Tests.Queries
{
    [TestFixture]
    public class QueryTests
    {
        private static IWebHost _testServer;
        private static IConfiguration _config;

        static QueryTests()
        {
            _config = OpenTraceabilityTests.GetConfiguration("appsettings.TestServer");
            _testServer = OpenTraceability.TestServer.WebServiceFactory.Create("https://localhost:4001", _config);
        }

        [Test]
        public void TestServer()
        {
            // do nothing, the static initializers does it all
        }

        [Test]
        public void QueryParameters()
        {
            EPCISQueryParameters parameters = new EPCISQueryParameters();
            parameters.query.MATCH_epc = new List<string>() { "https://id.gs1.org/01/00614141777778/10/987" };
            parameters.query.MATCH_anyEPC = new List<string>() { "https://id.gs1.org/01/00614141777778/10/987", "https://id.gs1.org/01/00614141777778/10/987" };
            parameters.query.MATCH_epcClass = new List<string>() { "urn:epc:class:lgtin:4012345.012345.998877" };
            parameters.query.MATCH_anyEPCClass = new List<string>() { "urn:epc:class:lgtin:4012345.012345.998877", "urn:epc:class:lgtin:4012345.012345.998877" };
            parameters.query.GE_eventTime = DateTime.UtcNow;
            parameters.query.GE_recordTime = DateTime.UtcNow;
            parameters.query.LE_eventTime = DateTime.UtcNow;
            parameters.query.LE_recordTime = DateTime.UtcNow;
            parameters.query.EQ_bizLocation = new List<Uri>() { new Uri("urn:epc:id:sgln:0614141.00888.0"), new Uri("urn:epc:id:sgln:0614141.00888.0") };
            parameters.query.EQ_bizStep = new List<string>() { "https://ref.gs1.org/cbv/BizStep-shipping", "receiving" };

            string queryParameters = parameters.ToQueryParameters();
            Uri uri = new Uri("https://example.org" + queryParameters);

            EPCISQueryParameters paramsAfter = new EPCISQueryParameters(uri);

            OpenTraceabilityTests.CompareJSON(parameters.ToJSON(), paramsAfter.ToJSON());
        }

        [Test]
        public void QueryParameters02()
        {
            EPCISQueryParameters parameters = new EPCISQueryParameters();
            parameters.query.MATCH_epc = new List<string>() { "https://id.gs1.org/01/00614141777778/10/987" };
            parameters.query.MATCH_anyEPC = new List<string>() { "https://id.gs1.org/01/00614141777778/10/987", "https://id.gs1.org/01/00614141777778/10/987" };
            parameters.query.MATCH_epcClass = new List<string>() { "urn:epc:class:lgtin:4012345.012345.998877" };
            parameters.query.MATCH_anyEPCClass = new List<string>() { "urn:epc:class:lgtin:4012345.012345.998877", "urn:epc:class:lgtin:4012345.012345.998877" };
            parameters.query.GE_eventTime = DateTime.UtcNow;
            parameters.query.GE_recordTime = DateTime.UtcNow;
            parameters.query.LE_eventTime = DateTime.UtcNow;
            parameters.query.LE_recordTime = DateTime.UtcNow;
            parameters.query.EQ_bizStep = new List<string>() { "https://ref.gs1.org/cbv/BizStep-shipping", "receiving" };

            string queryParameters = parameters.ToQueryParameters();
            Uri uri = new Uri("https://example.org" + queryParameters);

            EPCISQueryParameters paramsAfter = new EPCISQueryParameters(uri);

            OpenTraceabilityTests.CompareJSON(parameters.ToJSON(), paramsAfter.ToJSON());
        }

        [Test]
        [TestCase("epcisdocument-example01.jsonld")]
        public async Task LiveServerTests(string filename)
        {
            EPCISTestServerClient client = new EPCISTestServerClient("https://traceabilitytestserver01.azurewebsites.net", EPCISDataFormat.JSON, OpenTraceability.Models.Events.EPCISVersion.V2);

            // upload a blob of events
            string data = OpenTraceabilityTests.ReadTestData(filename);
            var doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(data);
            string blob_id = await client.Post(doc);

            // grab the traceability data...
            foreach (var e in doc.Events)
            {
                foreach (var p in e.Products)
                {
                    EPCISQueryParameters parameters = new EPCISQueryParameters(p.EPC);
                    var results = await client.QueryEvents(blob_id, parameters);
                    Assert.IsNotNull(results.Document);
                    Assert.That(results.Errors.Count, Is.EqualTo(0), "errors found in the query events");
                    Assert.That(results.Document.Events, Is.Not.Empty, "no events returned");

                    // grab the master data
                    await client.ResolveMasterData(blob_id, results.Document);
                    Assert.That(results.Document.MasterData.Count, Is.Not.EqualTo(0), "no master data resolved");
                }
            }
        }

        [Test]
        [TestCase("testserver_advancedfilters.jsonld")]
        public async Task MasterData(string filename)
        {
            EPCISTestServerClient client = new EPCISTestServerClient("https://localhost:4001", Mappers.EPCISDataFormat.JSON, Models.Events.EPCISVersion.V2);

            // upload a blob of events
            string data = OpenTraceabilityTests.ReadTestData(filename);
            var doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(data);
            string blob_id = await client.Post(doc);

            bool foundOneGDSTLocation = false;

            // grab the traceability data...
            foreach (var e in doc.Events)
            {
                foreach (var p in e.Products)
                {
                    EPCISQueryParameters parameters = new EPCISQueryParameters(p.EPC);
                    var results = await client.QueryEvents(blob_id, parameters);
                    Assert.IsNotNull(results.Document);
                    Assert.That(results.Errors.Count, Is.EqualTo(0), "errors found in the query events");
                    Assert.That(results.Document.Events, Is.Not.Empty, "no events returned");

                    // grab the master data
                    await client.ResolveMasterData(blob_id, results.Document);
                    Assert.That(results.Document.MasterData.Count, Is.Not.EqualTo(0), "no master data resolved");

                    if (results.Document.MasterData.Exists(m => m is GDSTLocation))
                    {
                        foundOneGDSTLocation = true;
                    }
                }
            }

            Assert.IsTrue(foundOneGDSTLocation, "Did not find GDSTLocation.");
        }

        [Test]
        [TestCase("testserver_advancedfilters.jsonld")]
        public async Task GetEPCISQueryInterfaceURL(string filename)
        {
            using HttpClientHandler httpClientHandler = new HttpClientHandler();
            httpClientHandler.ServerCertificateCustomValidationCallback += (m, e, c, h) =>
            {
                return true;
            };
            using HttpClient httpClient = new HttpClient(httpClientHandler);
            EPCISTestServerClient client = new EPCISTestServerClient("https://localhost:4001", Mappers.EPCISDataFormat.JSON, Models.Events.EPCISVersion.V2);

            // upload a blob of events
            string data = OpenTraceabilityTests.ReadTestData(filename);
            var doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(data);
            string blob_id = await client.Post(doc);

            DigitalLinkQueryOptions queryOptions = new DigitalLinkQueryOptions();
            queryOptions.URL = new Uri($"https://localhost:4001/digitallink/{blob_id}/");

            // grab the traceability data...
            foreach (var e in doc.Events)
            {
                foreach (var p in e.Products)
                {
                    var epcisQueryInterfaceURL = await EPCISTraceabilityResolver.GetEPCISQueryInterfaceURL(queryOptions, p.EPC, httpClient);
                    Assert.IsNotNull(epcisQueryInterfaceURL, $"Failed to get EPCIS URL for {p.EPC}");
                }
            }
        }

        [Test]
        [TestCase("aggregation_event_all_possible_fields.jsonld")]
        public async Task QueryEvents(string filename)
        {
            EPCISTestServerClient client = new EPCISTestServerClient("https://localhost:4001", Mappers.EPCISDataFormat.JSON, Models.Events.EPCISVersion.V2);

            // upload a blob of events
            string data = OpenTraceabilityTests.ReadTestData(filename);
            var doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(data);
            string blob_id = await client.Post(doc);

            // query for the events for each epc in the blob
            foreach (var e in doc.Events)
            {
                foreach (var p in e.Products)
                {
                    EPCISQueryParameters parameters = new EPCISQueryParameters(p.EPC);
                    var results = await client.QueryEvents(blob_id, parameters);
                    Assert.That(results.Errors.Count, Is.EqualTo(0), "errors found in the query events");
                    Assert.That(results.Document.Events.Count, Is.EqualTo(1), "no events returned");
                }
            }
        }

        [Test]
        [TestCase("testserver_advancedfilters.jsonld", "urn:epc:id:sscc:08600031303.0004", "urn:epcglobal:cbv:bizStep:receiving", "urn:gdst:example.org:location:loc:importer.123u")]
        public async Task AdvancedFilters(string filename, string epc, string bizStep, string bizLocation)
        {
            EPCISTestServerClient client = new EPCISTestServerClient("https://localhost:4001", Mappers.EPCISDataFormat.JSON, Models.Events.EPCISVersion.V2);

            // upload a blob of events
            string data = OpenTraceabilityTests.ReadTestData(filename);
            var doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(data);
            string blob_id = await client.Post(doc);

            // query for the events for each epc in the blob
            EPCISQueryParameters parameters = new EPCISQueryParameters(new Models.Identifiers.EPC(epc));
            parameters.query.EQ_bizStep = new List<string>() { bizStep };
            parameters.query.EQ_bizLocation = new List<Uri>() { new Uri(bizLocation) };

            var results = await client.QueryEvents(blob_id, parameters);
            Assert.That(results.Errors.Count, Is.EqualTo(0), "errors found in the query events");
            Assert.That(results.Document.Events.Count, Is.EqualTo(1), "no events returned");
        }

        [Test]
        [TestCase("traceback_tests.jsonld")]
        public async Task Traceback(string filename)
        {
            EPCISTestServerClient client = new EPCISTestServerClient("https://localhost:4001", Mappers.EPCISDataFormat.JSON, Models.Events.EPCISVersion.V2);

            // upload a blob of events
            string data = OpenTraceabilityTests.ReadTestData(filename);
            var doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(data);
            string blob_id = await client.Post(doc);

            var results = await client.Traceback(blob_id, new Models.Identifiers.EPC("urn:gdst:example.org:product:lot:class:processor.2u.v1-0122-2022"));
            Assert.That(results.Errors.Count, Is.EqualTo(0), "errors found in the traceback events");
            Assert.IsNotNull(results.Document);
            Assert.That(results.Document.Events.Count, Is.EqualTo(16), "expected 16 events");
        }

        [Test]
        [TestCase("epcisquerydoc-example01.jsonld")]
        public async Task Traceback02(string filename)
        {
            EPCISTestServerClient client = new EPCISTestServerClient("https://localhost:4001", Mappers.EPCISDataFormat.JSON, Models.Events.EPCISVersion.V2);

            // upload a blob of events
            string data = OpenTraceabilityTests.ReadTestData(filename);
            var doc = OpenTraceabilityMappers.EPCISQueryDocument.JSON.Map(data);
            string blob_id = await client.Post(doc.ToEPCISDocument());

            List<string> uniqueEventIDs = doc.Events.Select(e => e.EventID.ToString()).Distinct().ToList();

            var results = await client.Traceback(blob_id, new Models.Identifiers.EPC("urn:epc:id:sscc:08600031303.0003"));
            Assert.That(results.Errors.Count, Is.EqualTo(0), "errors found in the traceback events");
            Assert.IsNotNull(results.Document);
            Assert.That(results.Document.Events.Count, Is.EqualTo(18));

            var results2 = await client.Traceback(blob_id, new Models.Identifiers.EPC("urn:epc:id:sscc:0614141.1234567890"));
            Assert.That(results2.Errors.Count, Is.EqualTo(0), "errors found in the traceback events");
            Assert.IsNotNull(results2.Document);
            Assert.That(results2.Document.Events.Count, Is.EqualTo(13));
        }

        //[Test]
        //public async Task TracebackHarness()
        //{


        //    HttpClient client = new HttpClient();
        //    client.DefaultRequestHeaders.Add("X-API-Key", apiKey);
        //    client.DefaultRequestHeaders.Add("Accept", "*/*");

        //    var result = await EPCISTraceabilityResolver.GetEPCISQueryInterfaceURL(new DigitalLinkQueryOptions()
        //    {
        //        Format = EPCISDataFormat.JSON,
        //        EnableStackTrace = true,
        //        URL = new Uri(digitalLinkURL),
        //    }, new Models.Identifiers.EPC(epc), client);

        //    var epcisResults = await EPCISTraceabilityResolver.Traceback(new EPCISQueryInterfaceOptions()
        //    {
        //        URL = result,
        //        Version = Models.Events.EPCISVersion.V2,
        //        Format = EPCISDataFormat.JSON
        //    }, new Models.Identifiers.EPC(epc), client);
        //}
    }
}