using System.Collections.Generic;
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
    public class DigitalLinkService
    {
        public const string LinkTypeEpcis = "gs1:epcis";
        public const string LinkTypeMasterData = "gs1:masterData";

        /// <summary>
        /// Builds the digital links for an identifier. <paramref name="masterDataPath"/> is the
        /// relative master data path (e.g. <c>product/09506000134376</c>); when null no master data
        /// link is emitted (e.g. for SSCC). When <paramref name="datasetId"/> is supplied it is
        /// injected as a leading path segment so the returned links stay scoped to that dataset.
        /// </summary>
        public List<DigitalLink> BuildLinks(string baseUrl, string? masterDataPath, string? linkTypeFilter, string? datasetId = null)
        {
            string trimmed = baseUrl.TrimEnd('/');
            string prefix = string.IsNullOrWhiteSpace(datasetId) ? "" : "/" + datasetId.Trim('/');
            var links = new List<DigitalLink>();

            bool wantEpcis = linkTypeFilter == null || linkTypeFilter.ToLower() == LinkTypeEpcis.ToLower();
            bool wantMasterData = linkTypeFilter == null || linkTypeFilter.ToLower() == LinkTypeMasterData.ToLower();

            if (wantEpcis)
            {
                links.Add(new DigitalLink
                {
                    link = trimmed + prefix + "/epcis",
                    linkType = LinkTypeEpcis,
                    authRequired = true
                });
            }

            if (wantMasterData && masterDataPath != null)
            {
                links.Add(new DigitalLink
                {
                    link = trimmed + prefix + "/masterdata/" + masterDataPath.TrimStart('/'),
                    linkType = LinkTypeMasterData,
                    authRequired = true
                });
            }

            return links;
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
    }
}
