using System;
using System.ComponentModel.DataAnnotations;

namespace OpenTraceability.TestServer.Core.Data.Entities
{
    /// <summary>
    /// A dataset and the GDST modules it supports. The dataset record is the source of truth for
    /// module minification of the dataset's EPCIS/master data responses: a request scoped to a
    /// dataset (via the /{datasetId}/... route prefix) is served using this record's modules, so
    /// the tie between a dataset and its modular support survives without any request headers.
    /// </summary>
    public class DatasetRecord
    {
        [Key]
        public long Id { get; set; }

        public string DatasetId { get; set; } = string.Empty;

        /// <summary>
        /// Comma-separated configured module names (e.g. "Seafood,Wildcaught"). Empty = core-only.
        /// Expansion rules (Core always present; Wildcaught/Aquaculture imply Seafood) are applied
        /// at read time via ModuleSet.Parse.
        /// </summary>
        public string Modules { get; set; } = string.Empty;

        public string? Description { get; set; }

        public DateTime CreatedUtc { get; set; }

        public DateTime UpdatedUtc { get; set; }
    }
}
