using System.Threading.Tasks;
using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;
using OpenTraceability.Mappers;
using OpenTraceability.Queries;
using OpenTraceability.TestServer.Core.Data;
using OpenTraceability.TestServer.Core.Services;
using OpenTraceability.Utility;

namespace OpenTraceability.Tests.TestServer
{
    public class TraceabilityStoreTests
    {
        private sealed class Factory : IDbContextFactory<TraceabilityDbContext>
        {
            private readonly DbContextOptions<TraceabilityDbContext> _options;
            public Factory(DbContextOptions<TraceabilityDbContext> options) => _options = options;
            public TraceabilityDbContext CreateDbContext() => new TraceabilityDbContext(_options);
        }

        private static (TraceabilityStore store, SqliteConnection keepAlive) NewStore()
        {
            OpenTraceability.Setup.Initialize();
            OpenTraceability.GDST.Setup.Initialize();
            string cs = $"DataSource=store-{System.Guid.NewGuid():N};Mode=Memory;Cache=Shared";
            var keepAlive = new SqliteConnection(cs);
            keepAlive.Open();
            var options = new DbContextOptionsBuilder<TraceabilityDbContext>().UseSqlite(cs).Options;
            var store = new TraceabilityStore(new Factory(options));
            store.InitializeAsync().GetAwaiter().GetResult();
            return (store, keepAlive);
        }

        private static string LoadSampleDocument()
        {
            var loader = new EmbeddedResourceLoader();
            return loader.ReadString("OpenTraceability.Tests", "OpenTraceability.Tests.Data.gdst_data_withmasterdata.jsonld");
        }

        [Test]
        public async Task IngestAndQuery_RoundTripsEvents()
        {
            var (store, keepAlive) = NewStore();
            try
            {
                var ingestion = new IngestionService(store);
                int ingested = await ingestion.IngestEpcisDocumentAsync("default", LoadSampleDocument(), EPCISDataFormat.JSON, checkSchema: false);
                Assert.That(ingested, Is.GreaterThan(0), "should have ingested events");

                var doc = await store.QueryEventsAsync("default", new EPCISQueryParameters());
                Assert.That(doc.Events.Count, Is.EqualTo(ingested), "query should return all ingested events");
            }
            finally
            {
                keepAlive.Close();
            }
        }
    }
}
