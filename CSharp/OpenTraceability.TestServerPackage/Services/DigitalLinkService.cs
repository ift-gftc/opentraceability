using System.Collections.Generic;
using Newtonsoft.Json.Linq;
using OpenTraceability;
using OpenTraceability.Models.MasterData;

namespace OpenTraceability.TestServer.Core.Services
{
    /// <summary>
    /// The kind of GS1 identifier being resolved by the digital link resolver.
    /// </summary>
    public enum DigitalLinkIdentifierType
    {
        Product,
        Location,
        Party,
        SSCC,
        EpcClass,
        EpcInstance
    }

    /// <summary>
    /// Builds GS1 Digital Link responses pointing at this server's EPCIS query interface and
    /// master data endpoints. Pure logic shared by the controllers and the WireMock host.
    /// </summary>
    /// <remarks>
    /// Serves two response shapes so the server aligns with both resolver standards:
    /// the legacy flat <see cref="DigitalLink"/> array (<see cref="BuildLinks"/>) and the current
    /// RFC 9264 linkset (<see cref="BuildLinkset"/>). <see cref="ResolveTargetHref"/> selects a
    /// single target for the redirect behavior. The link relation types come from the shared
    /// <see cref="DigitalLinkVocab"/> constants so the client and server always agree.
    /// </remarks>
    public class DigitalLinkService
    {
        public const string LinkTypeEpcis = DigitalLinkVocab.EpcisCurie;
        public const string LinkTypeMasterData = DigitalLinkVocab.MasterDataCurie;

        /// <summary>
        /// Builds the legacy flat digital link array for an identifier. <paramref name="masterDataPath"/>
        /// is the relative master data path (e.g. <c>product/09506000134376</c>); when null no master
        /// data link is emitted (e.g. for SSCC). When <paramref name="datasetId"/> is supplied it is
        /// injected as a leading path segment so the returned links stay scoped to that dataset.
        /// </summary>
        public List<DigitalLink> BuildLinks(string baseUrl, string? masterDataPath, string? linkTypeFilter, string? datasetId = null)
        {
            (string epcisHref, string? masterDataHref) = ComputeHrefs(baseUrl, masterDataPath, datasetId);
            var links = new List<DigitalLink>();

            bool wantEpcis = linkTypeFilter == null || linkTypeFilter.ToLower() == LinkTypeEpcis.ToLower();
            bool wantMasterData = linkTypeFilter == null || linkTypeFilter.ToLower() == LinkTypeMasterData.ToLower();

            if (wantEpcis)
            {
                links.Add(new DigitalLink
                {
                    link = epcisHref,
                    linkType = LinkTypeEpcis,
                    authRequired = true
                });
            }

            if (wantMasterData && masterDataHref != null)
            {
                links.Add(new DigitalLink
                {
                    link = masterDataHref,
                    linkType = LinkTypeMasterData,
                    authRequired = true
                });
            }

            return links;
        }

        /// <summary>
        /// Builds an RFC 9264 linkset for an identifier. Emits the <c>epcis</c> link and, when a
        /// master data path is supplied, the <c>masterData</c> link, each keyed by its full GS1 Web
        /// Vocabulary URI, plus exactly one <c>defaultLink</c> (master data when available, otherwise
        /// epcis) as required by the standard.
        /// </summary>
        /// <param name="baseUrl">The scheme+host base this server serves from.</param>
        /// <param name="anchor">The decompressed digital link URI the links are anchored to.</param>
        /// <param name="masterDataPath">The relative master data path, or null when the identifier has no master data (e.g. SSCC).</param>
        /// <param name="datasetId">Optional dataset id injected as a leading path segment.</param>
        /// <returns>The linkset carrying a single anchor item.</returns>
        public Linkset BuildLinkset(string baseUrl, string anchor, string? masterDataPath, string? datasetId = null)
        {
            (string epcisHref, string? masterDataHref) = ComputeHrefs(baseUrl, masterDataPath, datasetId);

            var item = new LinksetItem { anchor = anchor };

            // The default link is master data when the entity has one, otherwise the EPCIS repository.
            // It is described by both gs1:defaultLink and its descriptive link type, per the standard.
            if (masterDataHref != null)
            {
                item.linkTypes[DigitalLinkVocab.DefaultLinkUri] = LinkArray(masterDataHref, "Default Master Data");
                item.linkTypes[DigitalLinkVocab.MasterDataUri] = LinkArray(masterDataHref, "Master Data");
                item.linkTypes[DigitalLinkVocab.EpcisUri] = LinkArray(epcisHref, "EPCIS Repository");
            }
            else
            {
                item.linkTypes[DigitalLinkVocab.DefaultLinkUri] = LinkArray(epcisHref, "Default EPCIS Repository");
                item.linkTypes[DigitalLinkVocab.EpcisUri] = LinkArray(epcisHref, "EPCIS Repository");
            }

            return new Linkset { linkset = new List<LinksetItem> { item } };
        }

        /// <summary>
        /// Resolves the single target href to redirect to for a specific link type request. When
        /// <paramref name="linkType"/> is null, returns the default link (master data when available,
        /// otherwise epcis). Returns null when the requested link type is not available for this
        /// identifier (the caller returns a 404).
        /// </summary>
        /// <param name="baseUrl">The scheme+host base this server serves from.</param>
        /// <param name="masterDataPath">The relative master data path, or null when the identifier has no master data.</param>
        /// <param name="linkType">The requested link type (compact CURIE or full URI), or null for the default link.</param>
        /// <param name="datasetId">Optional dataset id injected as a leading path segment.</param>
        /// <returns>The target href, or null when the requested link type is unavailable.</returns>
        public string? ResolveTargetHref(string baseUrl, string? masterDataPath, string? linkType, string? datasetId = null)
        {
            (string epcisHref, string? masterDataHref) = ComputeHrefs(baseUrl, masterDataPath, datasetId);

            // No specific type requested: redirect to the default link.
            if (string.IsNullOrWhiteSpace(linkType))
            {
                return masterDataHref ?? epcisHref;
            }

            string normalized = linkType!.ToLower();
            if (normalized == DigitalLinkVocab.EpcisCurie.ToLower() || normalized == DigitalLinkVocab.EpcisUri.ToLower())
            {
                return epcisHref;
            }
            if (normalized == DigitalLinkVocab.MasterDataCurie.ToLower() || normalized == DigitalLinkVocab.MasterDataUri.ToLower())
            {
                return masterDataHref;
            }
            if (normalized == DigitalLinkVocab.DefaultLinkCurie.ToLower() || normalized == DigitalLinkVocab.DefaultLinkUri.ToLower())
            {
                return masterDataHref ?? epcisHref;
            }

            return null;
        }

        public List<DigitalLink> ForProduct(string baseUrl, string gtin, string? linkTypeFilter, string? datasetId = null)
            => BuildLinks(baseUrl, $"product/{gtin}", linkTypeFilter, datasetId);

        public List<DigitalLink> ForLocation(string baseUrl, string gln, string? linkTypeFilter, string? datasetId = null)
            => BuildLinks(baseUrl, $"location/{gln}", linkTypeFilter, datasetId);

        public List<DigitalLink> ForParty(string baseUrl, string pgln, string? linkTypeFilter, string? datasetId = null)
            => BuildLinks(baseUrl, $"party/{pgln}", linkTypeFilter, datasetId);

        public List<DigitalLink> ForSSCC(string baseUrl, string sscc, string? linkTypeFilter, string? datasetId = null)
            => BuildLinks(baseUrl, null, linkTypeFilter, datasetId);

        public List<DigitalLink> ForEpcClass(string baseUrl, string gtin, string lot, string? linkTypeFilter, string? datasetId = null)
            => BuildLinks(baseUrl, $"product/{gtin}", linkTypeFilter, datasetId);

        public List<DigitalLink> ForEpcInstance(string baseUrl, string gtin, string serial, string? linkTypeFilter, string? datasetId = null)
            => BuildLinks(baseUrl, $"product/{gtin}", linkTypeFilter, datasetId);

        /// <summary>
        /// Computes the EPCIS and (optional) master data target hrefs for an identifier, honoring an
        /// optional dataset prefix. Centralized so the array, linkset, and redirect paths stay in sync.
        /// </summary>
        private static (string epcisHref, string? masterDataHref) ComputeHrefs(string baseUrl, string? masterDataPath, string? datasetId)
        {
            string trimmed = baseUrl.TrimEnd('/');
            string prefix = string.IsNullOrWhiteSpace(datasetId) ? "" : "/" + datasetId!.Trim('/');

            string epcisHref = trimmed + prefix + "/epcis";
            string? masterDataHref = masterDataPath != null
                ? trimmed + prefix + "/masterdata/" + masterDataPath.TrimStart('/')
                : null;

            return (epcisHref, masterDataHref);
        }

        /// <summary>
        /// Builds a linkset link relation type value: a JSON array holding a single link object with
        /// the mandatory <c>href</c> and <c>title</c>.
        /// </summary>
        private static JArray LinkArray(string href, string title)
        {
            return new JArray
            {
                new JObject
                {
                    ["href"] = href,
                    ["title"] = title
                }
            };
        }
    }
}
