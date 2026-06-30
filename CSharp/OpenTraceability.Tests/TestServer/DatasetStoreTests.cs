using System.Threading.Tasks;
using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;
using OpenTraceability.GDST;
using OpenTraceability.Mappers;
using OpenTraceability.Queries;
using OpenTraceability.TestServer.Core.Data;
using OpenTraceability.TestServer.Core.Models;
using OpenTraceability.TestServer.Core.Services;
using OpenTraceability.Utility;

namespace OpenTraceability.Tests.TestServer
{
    public class DatasetStoreTests
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
            string cs = $"DataSource=dataset-{System.Guid.NewGuid():N};Mode=Memory;Cache=Shared";
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
        public async Task DatasetCrud_RoundTrips()
        {
            var (store, keepAlive) = NewStore();
            try
            {
                Assert.That(await store.GetDatasetAsync("ds1"), Is.Null);

                var created = await store.UpsertDatasetAsync(new Dataset
                {
                    DatasetId = "ds1",
                    Modules = new() { "Seafood" },
                    Description = "first"
                });
                Assert.That(created.DatasetId, Is.EqualTo("ds1"));

                var fetched = await store.GetDatasetAsync("ds1");
                Assert.That(fetched, Is.Not.Null);
                Assert.That(fetched!.Modules, Is.EquivalentTo(new[] { "Seafood" }));
                Assert.That(fetched.GetExpandedModules(), Is.EquivalentTo(new[] { GdstModule.Core, GdstModule.Seafood }));
                Assert.That(fetched.Description, Is.EqualTo("first"));

                // update preserves CreatedUtc
                var updated = await store.UpsertDatasetAsync(new Dataset
                {
                    DatasetId = "ds1",
                    Modules = new() { "Seafood", "Wildcaught" },
                    Description = "second"
                });
                Assert.That(updated.CreatedUtc, Is.EqualTo(fetched.CreatedUtc));
                Assert.That(updated.Modules, Is.EquivalentTo(new[] { "Seafood", "Wildcaught" }));

                var all = await store.ListDatasetsAsync();
                Assert.That(all.Select(d => d.DatasetId), Does.Contain("ds1"));

                Assert.That(await store.DeleteDatasetAsync("ds1", purgeData: false), Is.True);
                Assert.That(await store.GetDatasetAsync("ds1"), Is.Null);
                Assert.That(await store.DeleteDatasetAsync("ds1", purgeData: false), Is.False);
            }
            finally
            {
                keepAlive.Close();
            }
        }

        [Test]
        public async Task ClearDatasetData_RemovesDataButKeepsRecord()
        {
            var (store, keepAlive) = NewStore();
            try
            {
                await store.UpsertDatasetAsync(new Dataset { DatasetId = "ds2", Modules = new() { "Wildcaught" } });

                var ingestion = new IngestionService(store);
                int ingested = await ingestion.IngestEpcisDocumentAsync("ds2", LoadSampleDocument(), EPCISDataFormat.JSON, checkSchema: false);
                Assert.That(ingested, Is.GreaterThan(0));

                await store.ClearDatasetDataAsync("ds2");

                var doc = await store.QueryEventsAsync("ds2", new EPCISQueryParameters());
                Assert.That(doc.Events, Is.Empty, "events should be purged");
                Assert.That(await store.GetDatasetAsync("ds2"), Is.Not.Null, "dataset record should remain");
            }
            finally
            {
                keepAlive.Close();
            }
        }

        [Test]
        public async Task InitializeAsync_UpgradesDatabaseMissingDatasetsTable()
        {
            // Simulates a pre-upgrade epcis.db: the data tables exist (EnsureCreated will no-op)
            // but the Datasets table does not, so InitializeAsync's raw DDL must add it.
            var (store, keepAlive) = NewStore();
            try
            {
                using (var drop = keepAlive.CreateCommand())
                {
                    drop.CommandText = "DROP TABLE \"Datasets\"";
                    drop.ExecuteNonQuery();
                }

                await store.InitializeAsync();

                var dataset = await store.UpsertDatasetAsync(new Dataset { DatasetId = "upgraded", Modules = new() { "Aquaculture" } });
                Assert.That(dataset.DatasetId, Is.EqualTo("upgraded"));
                var fetched = await store.GetDatasetAsync("upgraded");
                Assert.That(fetched, Is.Not.Null);
                Assert.That(fetched!.GetExpandedModules(), Does.Contain(GdstModule.Seafood), "Aquaculture implies Seafood");
            }
            finally
            {
                keepAlive.Close();
            }
        }
    }
}
