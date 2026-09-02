namespace OpenTraceability
{
    /// <summary>
    /// GS1 Web Vocabulary link relation types and media types used by the GS1 Digital Link
    /// resolver, in both the compact CURIE form (<c>gs1:epcis</c>) used by the legacy
    /// <c>?linkType</c> query parameter and the fully expanded URI form
    /// (<c>https://ref.gs1.org/voc/epcis</c>) used as the keys of an RFC 9264 linkset.
    /// </summary>
    /// <remarks>
    /// These values were previously duplicated as inline string literals across the resolvers and
    /// the test server. They are consolidated here so the client, the diagnostics rules, and the
    /// test traceability server all agree on the exact link types. Only the link types relevant to
    /// this slim EPCIS/GDST profile are included (<c>epcis</c>, <c>masterData</c>, and the
    /// mandatory <c>defaultLink</c>).
    /// </remarks>
    public static class DigitalLinkVocab
    {
        /// <summary>The base URI of the GS1 Web Vocabulary that all link type URIs expand from.</summary>
        public const string VocBaseUri = "https://ref.gs1.org/voc/";

        // Compact CURIE forms, as used in the legacy ?linkType query parameter.

        /// <summary>The compact link type for an EPCIS query interface (<c>gs1:epcis</c>).</summary>
        public const string EpcisCurie = "gs1:epcis";

        /// <summary>The compact link type for a master data resource (<c>gs1:masterData</c>).</summary>
        public const string MasterDataCurie = "gs1:masterData";

        /// <summary>The compact link type for the mandatory default link (<c>gs1:defaultLink</c>).</summary>
        public const string DefaultLinkCurie = "gs1:defaultLink";

        /// <summary>
        /// The reserved <c>linkType</c> value a client uses to request the entity's linkset
        /// instead of being redirected.
        /// </summary>
        public const string LinksetLinkType = "linkset";

        // Fully expanded URI forms, as used for the keys of an RFC 9264 linkset.

        /// <summary>The full URI link type for an EPCIS query interface.</summary>
        public const string EpcisUri = VocBaseUri + "epcis";

        /// <summary>The full URI link type for a master data resource.</summary>
        public const string MasterDataUri = VocBaseUri + "masterData";

        /// <summary>The full URI link type for the mandatory default link.</summary>
        public const string DefaultLinkUri = VocBaseUri + "defaultLink";

        /// <summary>
        /// The media type a client sets on its <c>Accept</c> header (or a resolver sets on its
        /// response <c>Content-Type</c>) to request/return an RFC 9264 linkset.
        /// </summary>
        public const string LinksetMediaType = "application/linkset+json";
    }
}
