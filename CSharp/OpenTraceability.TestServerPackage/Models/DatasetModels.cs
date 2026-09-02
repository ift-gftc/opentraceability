using System;
using System.Collections.Generic;
using System.Linq;
using OpenTraceability.GDST.Modules;

namespace OpenTraceability.TestServer.Core.Models
{
    /// <summary>A dataset as exposed by the /datasets management API.</summary>
    public class DatasetModel
    {
        public string DatasetId { get; set; } = string.Empty;

        /// <summary>The configured module names (Seafood, Wildcaught, Aquaculture). Empty = core-only.</summary>
        public List<string> Modules { get; set; } = new List<string>();

        /// <summary>
        /// The effective module set after expansion (Core always present; Wildcaught/Aquaculture
        /// imply Seafood). Read-only.
        /// </summary>
        public List<string> ExpandedModules =>
            ModuleSet.Parse(Modules).OrderBy(m => (int)m).Select(m => m.ToString()).ToList();

        public string? Description { get; set; }

        public DateTime CreatedUtc { get; set; }

        public DateTime UpdatedUtc { get; set; }

        public static DatasetModel FromDataset(Dataset dataset) => new DatasetModel
        {
            DatasetId = dataset.DatasetId,
            Modules = dataset.Modules,
            Description = dataset.Description,
            CreatedUtc = dataset.CreatedUtc,
            UpdatedUtc = dataset.UpdatedUtc
        };
    }

    /// <summary>The body for PUT /datasets/{datasetId} (idempotent create-or-update).</summary>
    public class UpsertDatasetRequest
    {
        /// <summary>Module names: Seafood, Wildcaught, Aquaculture. Empty/omitted = core-only.</summary>
        public List<string> Modules { get; set; } = new List<string>();

        public string? Description { get; set; }
    }
}
