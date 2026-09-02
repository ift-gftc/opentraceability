namespace OpenTraceability.Queries
{
    /// <summary>
    /// The version of the GS1 Digital Link / Resolver standard that a digital link resolver
    /// is expected to conform to when resolving EPCIS and master data links.
    /// </summary>
    /// <remarks>
    /// The two versions differ in the shape of the resolver response and therefore in how the
    /// SDK extracts a target URL:
    /// <list type="bullet">
    /// <item><description><see cref="ResolverStandard_1_1_2"/> — the legacy behavior, where the
    /// resolver returns a flat JSON array of digital link objects filtered by a
    /// <c>?linkType=gs1:xxx</c> query parameter, and the SDK takes the first matching link.</description></item>
    /// <item><description><see cref="ResolverStandard_1_2_0"/> — the current standard
    /// (RFC 9264 linkset), where the SDK requests the entity's linkset
    /// (<c>Accept: application/linkset+json</c>) and extracts the target href from the link
    /// relation type keyed by its full GS1 Web Vocabulary URI.</description></item>
    /// </list>
    /// The SDK defaults to <see cref="ResolverStandard_1_2_0"/> so new consumers align with the
    /// latest standard without opting in.
    /// </remarks>
    public enum ResolverVersion
    {
        /// <summary>
        /// The legacy resolver behavior: a flat JSON array of digital link objects selected via
        /// the <c>?linkType</c> query parameter.
        /// </summary>
        ResolverStandard_1_1_2,

        /// <summary>
        /// The current GS1-Conformant Resolver standard: an RFC 9264 linkset requested via the
        /// <c>application/linkset+json</c> media type and parsed client-side.
        /// </summary>
        ResolverStandard_1_2_0
    }
}
