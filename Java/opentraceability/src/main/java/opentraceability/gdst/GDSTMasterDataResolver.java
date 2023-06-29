package opentraceability.gdst;

import com.squareup.okhttp.OkHttpClient;
import opentraceability.gdst.events.IGDSTEvent;
import opentraceability.models.events.EPCISBaseDocument;
import opentraceability.queries.DigitalLinkQueryOptions;
import opentraceability.queries.MasterDataResolver;

import java.net.http.HttpClient;
import java.util.concurrent.CompletableFuture;

public class GDSTMasterDataResolver
{
    public static void ResolveGDSTMasterData(DigitalLinkQueryOptions options, EPCISBaseDocument doc, OkHttpClient client) throws Exception {
        MasterDataResolver.resolveMasterData(options, doc, client);
        for (var evt : doc.events) {
            if (evt instanceof IGDSTEvent) {
                IGDSTEvent gdstEvent = (IGDSTEvent) evt;
                MasterDataResolver.resolveTradingParty(options, gdstEvent.getProductOwner(), doc, client);
                MasterDataResolver.resolveTradingParty(options, evt.informationProvider, doc, client);
            }
        }
    }
}
