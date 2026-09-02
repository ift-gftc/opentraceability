using System.Collections.Generic;
using Newtonsoft.Json;

namespace OpenTraceability.Models.MasterData
{
    /// <summary>
    /// The top-level response returned by a GS1-Conformant resolver when a linkset is requested,
    /// as defined by RFC 9264 (<c>application/linkset+json</c>).
    /// </summary>
    /// <remarks>
    /// A linkset carries one <see cref="LinksetItem"/> per anchor (identified entity). The SDK uses
    /// it as the current-standard replacement for the legacy flat <see cref="DigitalLink"/> array:
    /// rather than asking the resolver to filter by <c>?linkType</c> and redirect, the client
    /// fetches the whole linkset and selects the target link relation type itself.
    /// </remarks>
    public class Linkset
    {
        /// <summary>
        /// The set of linkset items, one per resolved anchor. In this slim profile a resolver
        /// typically returns a single item for the requested entity.
        /// </summary>
        [JsonProperty("linkset")]
        public List<LinksetItem> linkset { get; set; } = new List<LinksetItem>();
    }
}
