using System.Collections.Generic;
using Newtonsoft.Json;

namespace OpenTraceability.Models.MasterData
{
    /// <summary>
    /// A single link inside a linkset link relation type array: the target resource plus the
    /// metadata that lets a client choose between alternatives of the same type.
    /// </summary>
    /// <remarks>
    /// Per the GS1 resolver standard the target URL (<see cref="href"/>) and a human-readable
    /// <see cref="title"/> are mandatory; <see cref="type"/>, <see cref="hreflang"/>, and
    /// <see cref="context"/> are optional. This slim profile only reads <see cref="href"/>, but the
    /// remaining fields are modeled so the linkset round-trips without loss.
    /// </remarks>
    public class LinksetLink
    {
        /// <summary>The target URL of the link. Mandatory.</summary>
        [JsonProperty("href")]
        public string href { get; set; } = string.Empty;

        /// <summary>A human-readable title that can be displayed to end users. Mandatory.</summary>
        [JsonProperty("title")]
        public string? title { get; set; }

        /// <summary>The IANA media type of the target resource, when known. Optional.</summary>
        [JsonProperty("type")]
        public string? type { get; set; }

        /// <summary>The human language(s) of the target resource, as BCP 47 tags. Optional.</summary>
        [JsonProperty("hreflang")]
        public List<string>? hreflang { get; set; }

        /// <summary>Disambiguation context value(s) for the link. Optional.</summary>
        [JsonProperty("context")]
        public List<string>? context { get; set; }
    }
}
