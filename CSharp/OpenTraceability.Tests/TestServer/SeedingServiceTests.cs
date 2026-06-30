using System;
using System.IO;
using System.Threading.Tasks;
using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;
using OpenTraceability.Queries;
using OpenTraceability.TestServer.Core.Data;
using OpenTraceability.TestServer.Core.Models;
using OpenTraceability.TestServer.Core.Services;
using OpenTraceability.Utility;

namespace OpenTraceability.Tests.TestServer
{
    public class SeedingServiceTests
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
            string cs = $"DataSource=seed-{Guid.NewGuid():N};Mode=Memory;Cache=Shared";
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

        private static string NewSeedRoot()
        {
            string root = Path.Combine(Path.GetTempPath(), "ot-seed-tests-" + Guid.NewGuid().ToString("N"));
            Directory.CreateDirectory(root);
            return root;
        }

        [Test]
        public async Task Manifest_CreatesDatasetAndIngestsSharedFiles()
        {
            var (store, keepAlive) = NewStore();
            string root = NewSeedRoot();
            try
            {
                Directory.CreateDirectory(Path.Combine(root, "_shared"));
                File.WriteAllText(Path.Combine(root, "_shared", "doc.jsonld"), LoadSampleDocument());
                Directory.CreateDirectory(Path.Combine(root, "tier1"));
                File.WriteAllText(Path.Combine(root, "tier1", "dataset.json"),
                    "{ \"modules\": [\"Seafood\"], \"description\": \"tier one\", \"files\": [\"_shared/doc.jsonld\"] }");

                var seeder = new SeedingService(new IngestionService(store), store);
                await seeder.SeedFromDirectoryAsync(root);

                var dataset = await store.GetDatasetAsync("tier1");
                Assert.That(dataset, Is.Not.Null);
                Assert.That(dataset!.Modules, Is.EquivalentTo(new[] { "Seafood" }));
                Assert.That(dataset.Description, Is.EqualTo("tier one"));

                var doc = await store.QueryEventsAsync("tier1", new EPCISQueryParameters());
                Assert.That(doc.Events.Count, Is.GreaterThan(0), "shared file should be ingested into tier1");

                Assert.That(await store.GetDatasetAsync("_shared"), Is.Null, "_-prefixed folders are not datasets");
            }
            finally
            {
                keepAlive.Close();
                Directory.Delete(root, recursive: true);
            }
        }

        [Test]
        public async Task NoManifest_UsesDefaultModules_AndPreservesApiEdits()
        {
            var (store, keepAlive) = NewStore();
            string root = NewSeedRoot();
            try
            {
                Directory.CreateDirectory(Path.Combine(root, "plain"));
                File.WriteAllText(Path.Combine(root, "plain", "events.jsonld"), LoadSampleDocument());

                var seeder = new SeedingService(new IngestionService(store), store);
                await seeder.SeedFromDirectoryAsync(root, defaultModules: new[] { "Wildcaught" });

                var dataset = await store.GetDatasetAsync("plain");
                Assert.That(dataset, Is.Not.Null);
                Assert.That(dataset!.Modules, Is.EquivalentTo(new[] { "Wildcaught" }));

                // operator edits the modules via the management API...
                await store.UpsertDatasetAsync(new Dataset { DatasetId = "plain", Modules = new() { "Seafood" } });

                // ...and a restart (re-seed) must not overwrite them
                await seeder.SeedFromDirectoryAsync(root, defaultModules: new[] { "Wildcaught" });
                var after = await store.GetDatasetAsync("plain");
                Assert.That(after!.Modules, Is.EquivalentTo(new[] { "Seafood" }), "manifest-less reseed must not clobber API edits");
            }
            finally
            {
                keepAlive.Close();
                Directory.Delete(root, recursive: true);
            }
        }

        [Test]
        public async Task Manifest_WithInvalidModuleNames_SkipsDataset()
        {
            var (store, keepAlive) = NewStore();
            string root = NewSeedRoot();
            try
            {
                Directory.CreateDirectory(Path.Combine(root, "typo"));
                File.WriteAllText(Path.Combine(root, "typo", "dataset.json"),
                    "{ \"modules\": [\"Wildcatch\"] }"); // typo: not a valid module name

                var seeder = new SeedingService(new IngestionService(store), store);
                await seeder.SeedFromDirectoryAsync(root);

                Assert.That(await store.GetDatasetAsync("typo"), Is.Null,
                    "a manifest with unknown module names must not silently create a core-only dataset");
            }
            finally
            {
                keepAlive.Close();
                Directory.Delete(root, recursive: true);
            }
        }
    }
}
