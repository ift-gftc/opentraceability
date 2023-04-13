using OpenTraceability.GDST.Events;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Queries;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

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
