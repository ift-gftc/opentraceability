using System.Collections.Generic;

namespace OpenTraceability.TestServer.Core.Models
{
    /// <summary>The request body for triggering a GDST 2.0 capability test run.</summary>
    public class CapabilityTestRequest
    {
        /// <summary>The capability tool base URL (e.g. https://capabilitytool-beta-service.azurewebsites.net).</summary>
        public string ToolUrl { get; set; } = string.Empty;

        /// <summary>The X-API-Key to authenticate against the capability tool.</summary>
        public string ToolApiKey { get; set; } = string.Empty;

        /// <summary>Optional digital-link resolver URL of the tool used to fetch generated data. Defaults to {ToolUrl}/digitallink/.</summary>
        public string? ToolResolverUrl { get; set; }

        public string SolutionName { get; set; } = string.Empty;
        public string? Version { get; set; }
        public string Pgln { get; set; } = string.Empty;

        /// <summary>
        /// The dataset the capability test runs against. The dataset must exist; its persisted module
        /// set is sent to the capability tool and drives minification of every response the tool reads
        /// back through {BaseURL}/{datasetId}/digitallink/. The tool's generated data is stored here too.
        /// </summary>
        public string DatasetId { get; set; } = "default";

        /// <summary>
        /// When true, the dataset's events and master data are purged before the tool's generated data
        /// is fetched, so repeated runs don't accumulate stale EPCs. Seeded datasets are only restored
        /// at the next server startup — prefer purpose-made datasets for capability runs.
        /// </summary>
        public bool ClearDatasetBeforeRun { get; set; }

        /// <summary>Optional EPCs of the solution provider's own data to be validated.</summary>
        public List<string> SolutionProviderEPCs { get; set; } = new List<string>();
    }
}
