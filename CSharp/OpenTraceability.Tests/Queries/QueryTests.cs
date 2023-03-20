using System;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Configuration;
using OpenTraceability.Mappers;
using OpenTraceability.Queries;

namespace OpenTraceability.Tests.Queries
{
	[TestFixture]
	public class QueryTests
	{
		static IWebHost _testServer;
		static IConfiguration _config;

		static QueryTests()
		{
			_config = OpenTraceabilityTests.GetConfiguration("appsettings.TestServer");
			_testServer = OpenTraceability.TestServer.WebServiceFactory.Create("https://localhost:4001", _config);
        }

		[Test]
		public async Task TestServer()
		{
			// do nothing, the static initializers does it all
		}

		[Test]
        [TestCase("aggregation_event_all_possible_fields.jsonld")]
        public async Task QueryEvents(string filename)
		{
			EPCISTestServerClient client = new EPCISTestServerClient("https://localhost:4001", Mappers.EPCISDataFormat.JSON, Models.Events.EPCISVersion.V2, Guid.NewGuid().ToString());

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
					Assert.AreEqual(0, results.Errors.Count, "errors found in the query events");
				}
			}
		}

		[Test]
        [TestCase("aggregation_event_all_possible_fields.jsonld")]
        public async Task Traceback(string filename)
		{
            EPCISTestServerClient client = new EPCISTestServerClient("https://localhost:4001", Mappers.EPCISDataFormat.JSON, Models.Events.EPCISVersion.V2, Guid.NewGuid().ToString());

            // upload a blob of events
            string data = OpenTraceabilityTests.ReadTestData(filename);
            var doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(data);
            string blob_id = await client.Post(doc);

            // perform traceback for each epc in the blob
            foreach (var e in doc.Events)
            {
                foreach (var p in e.Products)
                {
                    var results = await client.Traceback(blob_id, p.EPC);
                    Assert.AreEqual(0, results.Errors.Count, "errors found in the traceback events");
                }
            }
        }
    }
}

