using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Configuration;
using OpenTraceability.GDST.MasterData;
using OpenTraceability.GDST.Queries;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Queries;

namespace OpenTraceability.Tests.Queries
{
    [TestFixture]
    [Category("UnitTest")]
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
            // The deprecated LE_ parameters must keep round-tripping for callers that still use them.
#pragma warning disable CS0618
            parameters.query.LE_eventTime = DateTime.UtcNow;
            parameters.query.LE_recordTime = DateTime.UtcNow;
#pragma warning restore CS0618
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
            // The deprecated LE_ parameters must keep round-tripping for callers that still use them.
#pragma warning disable CS0618
            parameters.query.LE_eventTime = DateTime.UtcNow;
            parameters.query.LE_recordTime = DateTime.UtcNow;
#pragma warning restore CS0618
            parameters.query.EQ_bizStep = new List<string>() { "https://ref.gs1.org/cbv/BizStep-shipping", "receiving" };

            string queryParameters = parameters.ToQueryParameters();
            Uri uri = new Uri("https://example.org" + queryParameters);

            EPCISQueryParameters paramsAfter = new EPCISQueryParameters(uri);

            OpenTraceabilityTests.CompareJSON(parameters.ToJSON(), paramsAfter.ToJSON());
        }

        /// <summary>
        /// The GDST 2.0 required parameters must survive a round trip through the query string.
        /// </summary>
        /// <remarks>
        /// LT_recordTime, LT_eventTime, and EQ_transformationID are newer than the reflection-driven
        /// serializer, so this pins that they serialize into the URL and parse back out again.
        /// </remarks>
        [Test]
        public void QueryParameters_StandardRequiredParameters_RoundTripThroughTheQueryString()
        {
            // Arrange
            EPCISQueryParameters parameters = new EPCISQueryParameters();
            parameters.query.MATCH_anyEPC = new List<string>() { "https://id.gs1.org/01/00614141777778/10/987" };
            parameters.query.MATCH_anyEPCClass = new List<string>() { "urn:epc:class:lgtin:4012345.012345.998877" };
            parameters.query.GE_eventTime = DateTime.UtcNow;
            parameters.query.LT_eventTime = DateTime.UtcNow.AddHours(1);
            parameters.query.GE_recordTime = DateTime.UtcNow;
            parameters.query.LT_recordTime = DateTime.UtcNow.AddHours(1);
            parameters.query.EQ_bizStep = new List<string>() { "https://ref.gs1.org/cbv/BizStep-shipping" };
            parameters.query.EQ_bizLocation = new List<Uri>() { new Uri("urn:epc:id:sgln:0614141.00888.0") };
            parameters.query.EQ_transformationID = new List<string>() { "transform-a", "transform-b" };

            // Act
            string queryParameters = parameters.ToQueryParameters();
            EPCISQueryParameters paramsAfter = new EPCISQueryParameters(new Uri("https://example.org" + queryParameters));

            // Assert
            Assert.That(queryParameters, Does.Contain("LT_eventTime"));
            Assert.That(queryParameters, Does.Contain("LT_recordTime"));
            Assert.That(queryParameters, Does.Contain("EQ_transformationID"));
            Assert.That(paramsAfter.query.LT_eventTime, Is.EqualTo(parameters.query.LT_eventTime));
            Assert.That(paramsAfter.query.LT_recordTime, Is.EqualTo(parameters.query.LT_recordTime));
            Assert.That(paramsAfter.query.EQ_transformationID, Is.EqualTo(parameters.query.EQ_transformationID));

            OpenTraceabilityTests.CompareJSON(parameters.ToJSON(), paramsAfter.ToJSON());
        }

        //[Test]
        //[TestCase("epcisdocument-example01.jsonld")]
        public async Task LiveServerTests(string filename)
        {
            EPCISTestServerClient client = new EPCISTestServerClient("https://traceabilitytestserver01.azurewebsites.net", "test", EPCISDataFormat.JSON, OpenTraceability.Models.Events.EPCISVersion.V2);

            // upload a blob of events
            string data = OpenTraceabilityTests.ReadTestData(filename);
            var doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(data);
            await client.Post(doc);

            // grab the traceability data...
            foreach (var e in doc.Events)
            {
                foreach (var p in e.Products)
                {
                    EPCISQueryParameters parameters = new EPCISQueryParameters(p.EPC);
                    var results = await client.QueryEvents(parameters);
                    Assert.That(results.Document, Is.Not.Null);
                    Assert.That(results.Errors.Count, Is.EqualTo(0), "errors found in the query events");
                    Assert.That(results.Document.Events, Is.Not.Empty, "no events returned");

                    // grab the master data
                    await client.ResolveMasterData(results.Document);
                    Assert.That(results.Document.MasterData.Count, Is.Not.EqualTo(0), "no master data resolved");
                }
            }
        }

        [Test]
        [TestCase("testserver_advancedfilters.jsonld")]
        public async Task MasterData(string filename)
        {
            EPCISTestServerClient client = new EPCISTestServerClient("https://localhost:4001", "test", Mappers.EPCISDataFormat.JSON, Models.Events.EPCISVersion.V2);

            // upload a blob of events
            string data = OpenTraceabilityTests.ReadTestData(filename);
            var doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(data);
            await client.Post(doc);

            bool foundOneGDSTLocation = false;

            // grab the traceability data...
            foreach (var e in doc.Events)
            {
                foreach (var p in e.Products)
                {
                    EPCISQueryParameters parameters = new EPCISQueryParameters(p.EPC);
                    var results = await client.QueryEvents(parameters);
                    Assert.That(results.Document, Is.Not.Null);
                    Assert.That(results.Errors.Count, Is.EqualTo(0), "errors found in the query events");
                    Assert.That(results.Document.Events, Is.Not.Empty, "no events returned");

                    // grab the master data
                    await client.ResolveMasterData(results.Document);
                    Assert.That(results.Document.MasterData.Count, Is.Not.EqualTo(0), "no master data resolved");

                    if (results.Document.MasterData.Exists(m => m is GDSTLocation))
                    {
                        foundOneGDSTLocation = true;
                    }
                }
            }

            Assert.That(foundOneGDSTLocation, Is.True, "Did not find GDSTLocation.");
        }

        [Test]
        [TestCase("testserver_advancedfilters.jsonld")]
        public async Task GDSTMasterData(string filename)
        {
            EPCISTestGDSTServerClient client = new EPCISTestGDSTServerClient("https://localhost:4001", "test", Mappers.EPCISDataFormat.JSON, Models.Events.EPCISVersion.V2);

            // upload a blob of events
            string data = OpenTraceabilityTests.ReadTestData(filename);
            var doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(data);
            await client.Post(doc);

            bool foundOneGDSTLocation = false;

            // grab the traceability data...
            foreach (var e in doc.Events)
            {
                foreach (var p in e.Products)
                {
                    EPCISQueryParameters parameters = new EPCISQueryParameters(p.EPC);
                    var results = await client.QueryEvents(parameters);
                    Assert.That(results.Document, Is.Not.Null);
                    Assert.That(results.Errors.Count, Is.EqualTo(0), "errors found in the query events");
                    Assert.That(results.Document.Events, Is.Not.Empty, "no events returned");

                    // grab the master data
                    await client.ResolveGDSTMasterData(results.Document);
                    Assert.That(results.Document.MasterData.Count, Is.Not.EqualTo(0), "no master data resolved");

                    if (results.Document.MasterData.Exists(m => m is GDSTLocation))
                    {
                        foundOneGDSTLocation = true;
                    }
                }
            }

            Assert.That(foundOneGDSTLocation, Is.True, "Did not find GDSTLocation.");
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
            EPCISTestServerClient client = new EPCISTestServerClient("https://localhost:4001", "test", Mappers.EPCISDataFormat.JSON, Models.Events.EPCISVersion.V2);

            // upload a blob of events
            string data = OpenTraceabilityTests.ReadTestData(filename);
            var doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(data);
            string blob_id = await client.Post(doc);

            DigitalLinkQueryOptions queryOptions = new DigitalLinkQueryOptions();
            queryOptions.URL = new Uri("https://localhost:4001/digitallink");
            queryOptions.APIKey = "test";
            queryOptions.Headers["X-Dataset-Id"] = blob_id;

            // grab the traceability data...
            foreach (var e in doc.Events)
            {
                foreach (var p in e.Products)
                {
                    var epcisQueryInterfaceURL = await EPCISTraceabilityResolver.GetEPCISQueryInterfaceURL(queryOptions, p.EPC, httpClient);
                    Assert.That(epcisQueryInterfaceURL, Is.Not.Null, $"Failed to get EPCIS URL for {p.EPC}");
                }
            }
        }

        [Test]
        [TestCase(ResolverVersion.ResolverStandard_1_1_2)]
        [TestCase(ResolverVersion.ResolverStandard_1_2_0)]
        public async Task GetEPCISQueryInterfaceURL_ByResolverVersion_ResolvesEpcisUrl(ResolverVersion resolverVersion)
        {
            // Arrange
            using HttpClientHandler httpClientHandler = new HttpClientHandler();
            httpClientHandler.ServerCertificateCustomValidationCallback += (m, e, c, h) => true;
            using HttpClient httpClient = new HttpClient(httpClientHandler);
            EPCISTestServerClient client = new EPCISTestServerClient("https://localhost:4001", "test", Mappers.EPCISDataFormat.JSON, Models.Events.EPCISVersion.V2);

            string data = OpenTraceabilityTests.ReadTestData("testserver_advancedfilters.jsonld");
            var doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(data);
            string blob_id = await client.Post(doc);

            DigitalLinkQueryOptions queryOptions = new DigitalLinkQueryOptions();
            queryOptions.URL = new Uri("https://localhost:4001/digitallink");
            queryOptions.APIKey = "test";
            queryOptions.Headers["X-Dataset-Id"] = blob_id;
            queryOptions.ResolverVersion = resolverVersion;

            // Act & Assert: both the legacy array path (1.1.2) and the linkset path (1.2.0) must
            // resolve to a non-null EPCIS query interface URL for every product.
            foreach (var e in doc.Events)
            {
                foreach (var p in e.Products)
                {
                    var epcisQueryInterfaceURL = await EPCISTraceabilityResolver.GetEPCISQueryInterfaceURL(queryOptions, p.EPC, httpClient);
                    Assert.That(epcisQueryInterfaceURL, Is.Not.Null, $"Failed to get EPCIS URL for {p.EPC} via {resolverVersion}");
                    Assert.That(epcisQueryInterfaceURL!.ToString(), Does.Contain("/epcis"));
                }
            }
        }

        [Test]
        [TestCase(ResolverVersion.ResolverStandard_1_1_2)]
        [TestCase(ResolverVersion.ResolverStandard_1_2_0)]
        public async Task ResolveMasterData_ByResolverVersion_ResolvesMasterData(ResolverVersion resolverVersion)
        {
            // Arrange
            EPCISTestServerClient client = new EPCISTestServerClient("https://localhost:4001", "test", Mappers.EPCISDataFormat.JSON, Models.Events.EPCISVersion.V2);

            string data = OpenTraceabilityTests.ReadTestData("testserver_advancedfilters.jsonld");
            var doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(data);
            string blob_id = await client.Post(doc);

            using HttpClientHandler httpClientHandler = new HttpClientHandler();
            httpClientHandler.ServerCertificateCustomValidationCallback += (m, e, c, h) => true;
            using HttpClient httpClient = new HttpClient(httpClientHandler);

            DigitalLinkQueryOptions queryOptions = new DigitalLinkQueryOptions();
            queryOptions.URL = new Uri("https://localhost:4001/digitallink");
            queryOptions.APIKey = "test";
            queryOptions.Headers["X-Dataset-Id"] = blob_id;
            queryOptions.ResolverVersion = resolverVersion;

            // Act
            await MasterDataResolver.ResolveMasterData(queryOptions, doc, httpClient);

            // Assert
            Assert.That(doc.MasterData.Count, Is.Not.EqualTo(0), $"no master data resolved via {resolverVersion}");
        }

        [Test]
        [TestCase("aggregation_event_all_possible_fields.jsonld")]
        public async Task QueryEvents(string filename)
        {
            EPCISTestServerClient client = new EPCISTestServerClient("https://localhost:4001", "test", Mappers.EPCISDataFormat.JSON, Models.Events.EPCISVersion.V2);

            // upload a blob of events
            string data = OpenTraceabilityTests.ReadTestData(filename);
            var doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(data);
            await client.Post(doc);

            // query for the events for each epc in the blob
            foreach (var e in doc.Events)
            {
                foreach (var p in e.Products)
                {
                    EPCISQueryParameters parameters = new EPCISQueryParameters(p.EPC);
                    var results = await client.QueryEvents(parameters);
                    Assert.That(results.Errors.Count, Is.EqualTo(0), "errors found in the query events");
                    Assert.That(results.Document, Is.Not.Null);
                    Assert.That(results.Document.Events.Count, Is.EqualTo(1), "no events returned");
                }
            }
        }

        [Test]
        [TestCase("aggregation_event_all_possible_fields.jsonld")]
        public async Task QueryEventsByWildCardLotSerialNumber(string filename)
        {
            EPCISTestServerClient client = new EPCISTestServerClient("https://localhost:4001", "test", Mappers.EPCISDataFormat.JSON, Models.Events.EPCISVersion.V2);

            // upload a blob of events
            string data = OpenTraceabilityTests.ReadTestData(filename);
            var doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(data);
            await client.Post(doc);

            // query for the events for each epc in the blob
            foreach (var e in doc.Events)
            {
                foreach (var p in e.Products.Where(x => x.EPC.Type == EPCType.Instance || x.EPC.Type == EPCType.Class))
                {
                    EPCISQueryParameters parameters = new();

                    if(p.EPC.Type == EPCType.Class)
                    {
                        EPC epc = new EPC(EPCType.Class, p.EPC.GTIN, "*");
                        parameters.query.MATCH_anyEPCClass= new List<string>() { epc.ToString() };
                    }
                    else
                    {
                        EPC epc = new EPC(p.EPC.Type, p.EPC.GTIN, "*");
                        parameters.query.MATCH_anyEPC = new List<string>() { epc.ToString() };
                    }

                    var results = await client.QueryEvents(parameters);
                    Assert.That(results.Errors.Count, Is.EqualTo(0), "errors found in the query events");
                    Assert.That(results.Document, Is.Not.Null);
                    Assert.That(results.Document.Events.Count, Is.EqualTo(1), "no events returned");
                }
            }
        }

        [Test]
        [TestCase("testserver_advancedfilters.jsonld", "urn:epc:id:sscc:08600031303.0004", "urn:epcglobal:cbv:bizStep:receiving", "urn:gdst:example.org:location:loc:importer.123u")]
        public async Task AdvancedFilters(string filename, string epc, string bizStep, string bizLocation)
        {
            EPCISTestServerClient client = new EPCISTestServerClient("https://localhost:4001", "test", Mappers.EPCISDataFormat.JSON, Models.Events.EPCISVersion.V2);

            // upload a blob of events
            string data = OpenTraceabilityTests.ReadTestData(filename);
            var doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(data);
            await client.Post(doc);

            // query for the events for each epc in the blob
            EPCISQueryParameters parameters = new EPCISQueryParameters(new EPC(epc));
            parameters.query.EQ_bizStep = new List<string>() { bizStep };
            parameters.query.EQ_bizLocation = new List<Uri>() { new Uri(bizLocation) };

            var results = await client.QueryEvents(parameters);
            Assert.That(results.Errors.Count, Is.EqualTo(0), "errors found in the query events");
            Assert.That(results.Document, Is.Not.Null);
            Assert.That(results.Document.Events.Count, Is.EqualTo(1), "no events returned");
        }

        [Test]
        [TestCase("traceback_tests.jsonld")]
        public async Task Traceback(string filename)
        {
            EPCISTestServerClient client = new EPCISTestServerClient("https://localhost:4001", "test", Mappers.EPCISDataFormat.JSON, Models.Events.EPCISVersion.V2);

            // upload a blob of events
            string data = OpenTraceabilityTests.ReadTestData(filename);
            var doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(data);
            await client.Post(doc);

            var results = await client.Traceback(new EPC("urn:gdst:example.org:product:lot:class:processor.2u.v1-0122-2022"));
            Assert.That(results.Errors.Count, Is.EqualTo(0), "errors found in the traceback events");
            Assert.That(results.Document, Is.Not.Null);
            Assert.That(results.Document.Events.Count, Is.EqualTo(16), "expected 16 events");
        }

        [Test]
        [TestCase("epcisquerydoc-example01.jsonld")]
        public async Task Traceback02(string filename)
        {
            EPCISTestServerClient client = new EPCISTestServerClient("https://localhost:4001", "test", Mappers.EPCISDataFormat.JSON, Models.Events.EPCISVersion.V2);

            // upload a blob of events
            string data = OpenTraceabilityTests.ReadTestData(filename);
            var doc = OpenTraceabilityMappers.EPCISQueryDocument.JSON.Map(data);
            await client.Post(doc.ToEPCISDocument());

            List<string> uniqueEventIDs = doc.Events.Select(e => e.EventID.ToString()).Distinct().ToList();

            var results = await client.Traceback(new EPC("urn:epc:id:sscc:08600031303.0003"));
            Assert.That(results.Errors.Count, Is.EqualTo(0), "errors found in the traceback events");
            Assert.That(results.Document, Is.Not.Null);
            Assert.That(results.Document.Events.Count, Is.EqualTo(18));

            var results2 = await client.Traceback(new EPC("urn:epc:id:sscc:0614141.1234567890"));
            Assert.That(results2.Errors.Count, Is.EqualTo(0), "errors found in the traceback events");
            Assert.That(results2.Document, Is.Not.Null);
            Assert.That(results2.Document.Events.Count, Is.EqualTo(13));
        }
    }
}