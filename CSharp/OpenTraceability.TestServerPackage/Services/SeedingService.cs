using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using Newtonsoft.Json;
using OpenTraceability.Mappers;
using OpenTraceability.TestServer.Core.Data;
using OpenTraceability.TestServer.Core.Models;

namespace OpenTraceability.TestServer.Core.Services
{
    /// <summary>
    /// Seeds the traceability store from a directory of datasets on startup. Each immediate
    /// subdirectory becomes a dataset whose id equals the folder name, and every file inside the
    /// folder is ingested into that dataset. A folder may carry a "dataset.json" manifest declaring
    /// the dataset's modules, a description, and extra data files shared between datasets:
    ///
    ///   { "modules": ["Seafood", "Wildcaught"], "description": "...", "files": ["_shared/doc.json"] }
    ///
    /// Folders starting with "_" hold shared assets and are not datasets themselves. Re-running is
    /// safe: the store upserts by dataset id + event/element id.
    /// </summary>
    public class SeedingService
    {
        private readonly IngestionService _ingestion;
        private readonly ITraceabilityStore _store;

        public SeedingService(IngestionService ingestion, ITraceabilityStore store)
        {
            _ingestion = ingestion;
            _store = store;
        }

        private class SeedManifest
        {
            [JsonProperty("modules")]
            public List<string> Modules { get; set; } = new List<string>();

            [JsonProperty("description")]
            public string? Description { get; set; }

            /// <summary>Extra data files to ingest, relative to the SeedData root (e.g. "_shared/doc.json").</summary>
            [JsonProperty("files")]
            public List<string> Files { get; set; } = new List<string>();
        }

        /// <summary>
        /// Scans <paramref name="rootPath"/> for dataset folders, upserts each dataset's record and
        /// ingests its files. Folders with a "dataset.json" manifest get the manifest's modules
        /// (the manifest wins over an existing record); folders without one get
        /// <paramref name="defaultModules"/>, but only when no record exists yet, so module edits
        /// made through the management API survive restarts. Missing root is a no-op. Individual
        /// file failures are swallowed so one bad seed file cannot abort startup.
        /// </summary>
        public async Task SeedFromDirectoryAsync(string rootPath, IEnumerable<string>? defaultModules = null)
        {
            if (string.IsNullOrWhiteSpace(rootPath) || !Directory.Exists(rootPath))
            {
                return;
            }

            foreach (string datasetDir in Directory.EnumerateDirectories(rootPath))
            {
                string datasetId = Path.GetFileName(datasetDir);
                if (string.IsNullOrEmpty(datasetId) || datasetId.StartsWith("_"))
                {
                    continue;
                }

                try
                {
                    await SeedDatasetAsync(rootPath, datasetDir, datasetId, defaultModules);
                }
                catch (Exception ex)
                {
                    // Never let a single malformed dataset folder take down the server.
                    Console.Error.WriteLine($"[SeedingService] Failed to seed dataset '{datasetId}': {ex.Message}");
                }
            }
        }

        private async Task SeedDatasetAsync(string rootPath, string datasetDir, string datasetId, IEnumerable<string>? defaultModules)
        {
            string manifestPath = Path.Combine(datasetDir, "dataset.json");
            var extraFiles = new List<string>();

            if (File.Exists(manifestPath))
            {
                var manifest = JsonConvert.DeserializeObject<SeedManifest>(await File.ReadAllTextAsync(manifestPath)) ?? new SeedManifest();
                if (!ModuleNames.TryParseStrict(manifest.Modules, out var modules, out var invalid))
                {
                    Console.Error.WriteLine($"[SeedingService] Dataset '{datasetId}' manifest has unknown module names ({string.Join(", ", invalid)}); skipping dataset.");
                    return;
                }

                // The manifest is the declared source of truth for this dataset's modules.
                await _store.UpsertDatasetAsync(new Dataset
                {
                    DatasetId = datasetId,
                    Modules = modules,
                    Description = manifest.Description
                });

                extraFiles.AddRange(manifest.Files.Select(f => Path.Combine(rootPath, f)));
            }
            else if (await _store.GetDatasetAsync(datasetId) == null)
            {
                ModuleNames.TryParseStrict(defaultModules, out var modules, out _);
                await _store.UpsertDatasetAsync(new Dataset
                {
                    DatasetId = datasetId,
                    Modules = modules
                });
            }

            var dataFiles = Directory.EnumerateFiles(datasetDir)
                .Where(f => !string.Equals(Path.GetFileName(f), "dataset.json", StringComparison.OrdinalIgnoreCase))
                .Concat(extraFiles);

            foreach (string file in dataFiles)
            {
                try
                {
                    await SeedFileAsync(datasetId, file);
                }
                catch (Exception ex)
                {
                    // Never let a single malformed seed file take down the server.
                    Console.Error.WriteLine($"[SeedingService] Failed to seed '{file}' into dataset '{datasetId}': {ex.Message}");
                }
            }
        }

        private async Task SeedFileAsync(string datasetId, string file)
        {
            string content = await File.ReadAllTextAsync(file);
            if (string.IsNullOrWhiteSpace(content))
            {
                return;
            }

            EPCISDataFormat format = file.EndsWith(".xml", StringComparison.OrdinalIgnoreCase)
                ? EPCISDataFormat.XML
                : EPCISDataFormat.JSON;

            // Route full EPCIS documents through the document ingester (events + header master data);
            // anything else is treated as standalone GS1 Web Vocab master data.
            if (content.Contains("EPCISDocument", StringComparison.OrdinalIgnoreCase) ||
                content.Contains("EPCISQueryDocument", StringComparison.OrdinalIgnoreCase))
            {
                await _ingestion.IngestEpcisDocumentAsync(datasetId, content, format, checkSchema: false);
            }
            else
            {
                await _ingestion.IngestMasterDataAsync(datasetId, content);
            }
        }
    }
}
