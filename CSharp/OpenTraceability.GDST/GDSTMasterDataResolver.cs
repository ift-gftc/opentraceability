using OpenTraceability.GDST.Events;
using OpenTraceability.GDST.MasterData;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.MasterData;
using OpenTraceability.Queries;

namespace OpenTraceability.GDST
{
    public static class GDSTMasterDataResolver
    {
        public static async Task ResolveGDSTMasterData(DigitalLinkQueryOptions options, EPCISBaseDocument doc, HttpClient client)
        {
            await MasterDataResolver.ResolveMasterData<GDSTTradeItem, GDSTLocation, TradingParty>(options, doc, client);

            foreach (var evt in doc.Events.OfType<IGDSTEvent>())
            {
                if (evt.InformationProvider is not null)
                {
                    await MasterDataResolver.ResolveTradingParty(options, evt.InformationProvider, doc, client);
                }

                if (evt is IGDSTProductOwnerEvent productOwnerEvent && productOwnerEvent.ProductOwner is not null)
                {
                    await MasterDataResolver.ResolveTradingParty(options, productOwnerEvent.ProductOwner, doc, client);
                }
            }
        }
    }
}
