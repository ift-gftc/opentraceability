using OpenTraceability.GDST.Events;
using OpenTraceability.GDST.MasterData;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.MasterData;
using OpenTraceability.Queries;
using OpenTraceability.Queries.Diagnostics;

namespace OpenTraceability.GDST
{
    public static class GDSTMasterDataResolver
    {
        /// <summary>
        /// Resolves the master data referenced by the document's events through the digital link
        /// resolver, using the GDST master data types, including the GDST-specific trading parties
        /// (information providers and product owners) the generic resolver does not know about.
        /// </summary>
        /// <param name="options">The digital link resolver options.</param>
        /// <param name="doc">The document whose events reference the master data to resolve.</param>
        /// <param name="client">The HTTP client to use for all requests.</param>
        /// <param name="report">Optional diagnostics report that records every request performed.</param>
        public static async Task ResolveGDSTMasterData(DigitalLinkQueryOptions options, EPCISBaseDocument doc, HttpClient client, DiagnosticsReport? report = null)
        {
            await MasterDataResolver.ResolveMasterData<GDSTTradeItem, GDSTLocation, TradingParty>(options, doc, client, report);

            foreach (var evt in doc.Events.OfType<IGDSTEvent>())
            {
                if (evt.InformationProvider is not null)
                {
                    await MasterDataResolver.ResolveTradingParty(options, evt.InformationProvider, doc, client, report: report);
                }

                if (evt is IGDSTProductOwnerEvent productOwnerEvent && productOwnerEvent.ProductOwner is not null)
                {
                    await MasterDataResolver.ResolveTradingParty(options, productOwnerEvent.ProductOwner, doc, client, report: report);
                }
            }
        }
    }
}
