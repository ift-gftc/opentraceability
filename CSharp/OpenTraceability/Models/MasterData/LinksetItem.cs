using System.Collections.Generic;
using System.Linq;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace OpenTraceability.Models.MasterData
{
    /// <summary>
    /// One entry in a linkset: the links available for a single anchor (identified entity),
    /// keyed by their fully expanded GS1 Web Vocabulary link relation type URI.
    /// </summary>
    /// <remarks>
    /// The link relation types are dynamic JSON keys (e.g. <c>https://ref.gs1.org/voc/epcis</c>),
    /// not fixed property names, so they are captured via <see cref="JsonExtensionData"/> into
    /// <see cref="linkTypes"/>. Use <see cref="GetLinks"/> to read the links for a given type by its
    /// full URI (see <see cref="OpenTraceability.DigitalLinkVocab"/>). The fixed <see cref="anchor"/>
    /// and <see cref="itemDescription"/> members are deserialized normally and therefore do not
    /// appear in the extension data.
    /// </remarks>
    public class LinksetItem
    {
        /// <summary>
        /// The decompressed GS1 Digital Link URI the links are anchored to
        /// (e.g. <c>https://id.gs1.org/01/09506000134352</c>).
        /// </summary>
        [JsonProperty("anchor")]
        public string anchor { get; set; } = string.Empty;

        /// <summary>An optional human-readable description of the anchored item.</summary>
        [JsonProperty("itemDescription")]
        public string? itemDescription { get; set; }

        /// <summary>
        /// The link relation types keyed by their full URI. Each value is the JSON array of links
        /// for that type. Populated from any JSON properties not mapped to a fixed member above.
        /// </summary>
        [JsonExtensionData]
        public IDictionary<string, JToken> linkTypes { get; set; } = new Dictionary<string, JToken>();

        /// <summary>
        /// Returns the links declared under the given link relation type URI, or an empty list when
        /// the anchor declares no link of that type.
        /// </summary>
        /// <param name="linkTypeUri">The fully expanded link type URI, e.g. <c>https://ref.gs1.org/voc/epcis</c>.</param>
        /// <returns>The links for that type, in declaration order; never null.</returns>
        public IReadOnlyList<LinksetLink> GetLinks(string linkTypeUri)
        {
            if (linkTypes != null && linkTypes.TryGetValue(linkTypeUri, out JToken? token) && token != null)
            {
                // A link relation type value is an array of link objects; tolerate a single object too.
                if (token.Type == JTokenType.Array)
                {
                    return token.ToObject<List<LinksetLink>>() ?? new List<LinksetLink>();
                }

                LinksetLink? single = token.ToObject<LinksetLink>();
                return single != null ? new List<LinksetLink> { single } : new List<LinksetLink>();
            }

            return new List<LinksetLink>();
        }
    }
}
