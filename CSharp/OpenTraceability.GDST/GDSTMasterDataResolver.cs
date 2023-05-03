using OpenTraceability.GDST.Events;
using OpenTraceability.Models.Events;
using OpenTraceability.Queries;

namespace OpenTraceability.GDST
{
    public static class GDSTMasterDataResolver
    {
        public static async Task ResolveGDSTMasterData(DigitalLinkQueryOptions options, EPCISBaseDocument doc, HttpClient client)
        {
            await MasterDataResolver.ResolveMasterData(options, doc, client);

            // find all master data we have not resolved yet...
            foreach (var evt in doc.Events)
            {
                if (evt is IGDSTEvent)
                {
                    IGDSTEvent gdstEvent = (IGDSTEvent)evt;
                    await MasterDataResolver.ResolveTradingParty(options, gdstEvent.InformationProvider, doc, client);
                    await MasterDataResolver.ResolveTradingParty(options, gdstEvent.ProductOwner, doc, client);
                }
            }
        }
    }
}