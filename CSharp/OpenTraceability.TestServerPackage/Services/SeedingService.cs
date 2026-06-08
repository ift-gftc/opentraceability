using System;
using System.IO;
using System.Threading.Tasks;
using OpenTraceability.Mappers;

namespace OpenTraceability.TestServer.Core.Services
{
    /// <summary>
    /// Seeds the traceability store from a directory of datasets on startup. Each immediate
    /// subdirectory becomes a dataset whose id equals the folder name, and every file inside the
    /// folder is ingested into that dataset. Re-running is safe: the store upserts by dataset id +
    /// event/element id.
    /// </summary>
    public class SeedingService
    {
        private readonly IngestionService _ingestion;

        public SeedingService(IngestionService ingestion)
        {
            _ingestion = ingestion;
        }

        /// <summary>
        /// Scans <paramref name="rootPath"/> for dataset folders and ingests their files. Each folder's
        /// name is used as the dataset id. Missing root is a no-op. Individual file failures are
        /// swallowed so one bad seed file cannot abort startup.
        /// </summary>
        public async Task SeedFromDirectoryAsync(string rootPath)
        {
            if (string.IsNullOrWhiteSpace(rootPath) || !Directory.Exists(rootPath))
            {
                return;
            }

            foreach (string datasetDir in Directory.EnumerateDirectories(rootPath))
            {
                string datasetId = Path.GetFileName(datasetDir);
                if (string.IsNullOrEmpty(datasetId))
                {
                    continue;
                }

                foreach (string file in Directory.EnumerateFiles(datasetDir))
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
