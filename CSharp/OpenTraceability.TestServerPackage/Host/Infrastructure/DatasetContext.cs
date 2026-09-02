using OpenTraceability.GDST;
using OpenTraceability.TestServer.Core.Models;

namespace OpenTraceability.TestServer.Infrastructure
{
    /// <summary>
    /// The dataset a request is scoped to, resolved once per request by
    /// <see cref="DatasetResolutionFilter"/>. The dataset's persisted module set drives module
    /// minification of every response, so the same deployed server can serve core-only data on one
    /// dataset and full seafood/wildcaught/aquaculture data on another.
    /// </summary>
    public class DatasetContext
    {
        public string DatasetId { get; set; } = "default";

        public Dataset Record { get; set; } = null!;

        /// <summary>The dataset's expanded module set (Core always present; Wildcaught/Aquaculture imply Seafood).</summary>
        public HashSet<GdstModule> Modules { get; set; } = new HashSet<GdstModule>();
    }
}
