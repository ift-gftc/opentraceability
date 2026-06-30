using System;
using System.Collections.Generic;
using OpenTraceability.GDST;
using OpenTraceability.GDST.Modules;

namespace OpenTraceability.TestServer.Core.Models
{
    /// <summary>
    /// A dataset and the GDST modules it supports. This is the domain view of the persisted
    /// dataset record and the source of truth for module minification of the dataset's data.
    /// </summary>
    public class Dataset
    {
        public string DatasetId { get; set; } = string.Empty;

        /// <summary>
        /// The configured module names (e.g. "Seafood", "Wildcaught"). Empty = core-only.
        /// </summary>
        public List<string> Modules { get; set; } = new List<string>();

        public string? Description { get; set; }

        public DateTime CreatedUtc { get; set; }

        public DateTime UpdatedUtc { get; set; }

        /// <summary>
        /// The full module set used for minification: Core always present, and
        /// Wildcaught/Aquaculture imply Seafood.
        /// </summary>
        public HashSet<GdstModule> GetExpandedModules() => ModuleSet.Parse(Modules);
    }
}
