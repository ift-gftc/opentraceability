package opentraceability.queries;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import opentraceability.interfaces.IEvent;
import opentraceability.interfaces.IVocabularyElement;
import opentraceability.mappers.OpenTraceabilityMappers;
import opentraceability.models.events.*;
import opentraceability.models.masterdata.DigitalLink;
import opentraceability.models.masterdata.Location;
import opentraceability.models.masterdata.TradeItem;
import opentraceability.models.masterdata.TradingParty;
import opentraceability.models.identifiers.*;
import opentraceability.utility.URLHelper;


import java.net.URI;
import java.util.List;

public class MasterDataResolver {
    public static void resolveMasterData(DigitalLinkQueryOptions options, EPCISBaseDocument doc, OkHttpClient client) throws Exception {
        for (IEvent evt : doc.events) {
            for (EventProduct p : evt.getProducts()) {
                if (p.EPC.getType() == EPCType.Class || p.EPC.getType() == EPCType.Instance) {
                    resolveTradeitem(options, p.EPC.getGTIN(), doc, client);
                }
            }

            resolveLocation(options, evt.location.gln, doc, client);

            for (EventSource source : evt.sourceList) {
                if (source.getParsedType() == EventSourceType.Owner) {
                    PGLN pgln = new PGLN(source.value);
                    if (pgln != null) {
                        resolveTradingParty(options, pgln, doc, client);
                    }
                }
            }

            for (EventDestination dest : evt.destinationList) {
                if (dest.getParsedType() == EventDestinationType.Owner) {
                    PGLN pgln = new PGLN(dest.value);
                    if (pgln != null) {
                        resolveTradingParty(options, pgln, doc, client);
                    }
                }
            }
        }
    }

    public static void resolveTradeitem(DigitalLinkQueryOptions options, GTIN gtin, EPCISBaseDocument doc, OkHttpClient client) throws Exception {
        if (gtin != null) {
            if (doc.searchMasterData(gtin.toString(), TradeItem.class) == null) {
                Class type = TradeItem.class;
                type = opentraceability.Setup.getMasterDataTypeDefault(type);
                IVocabularyElement ti = resolveMasterDataItem(type, options, "/01/" + gtin + "?linkType=gs1:masterData", client);
                if (ti != null) {
                    doc.masterData.add(ti);
                }
            }
        }
    }

    public static void resolveLocation(DigitalLinkQueryOptions options, GLN gln, EPCISBaseDocument doc, OkHttpClient client) throws Exception {
        if (gln != null) {
            if (doc.searchMasterData(gln.toString(), Location.class) == null) {
                Class type = Location.class;
                type = opentraceability.Setup.getMasterDataTypeDefault(type);
                IVocabularyElement l = resolveMasterDataItem(type, options, "/414/" + gln + "?linkType=gs1:masterData", client);
                if (l != null) {
                    doc.masterData.add(l);
                }
            }
        }
    }

    public static void resolveTradingParty(DigitalLinkQueryOptions options, PGLN pgln, EPCISBaseDocument doc, OkHttpClient client) throws Exception {
        if (pgln != null) {
            if (doc.searchMasterData(pgln.toString(), TradingParty.class) == null) {
                Class type = TradingParty.class;
                type = opentraceability.Setup.getMasterDataTypeDefault(type);
                IVocabularyElement tp = resolveMasterDataItem(type, options, "/417/" + pgln + "?linkType=gs1:masterData", client);
                if (tp != null) {
                    doc.masterData.add(tp);
                }
            }
        }
    }

    public static IVocabularyElement resolverMasterDataItem(DigitalLinkQueryOptions options, String relativeURL, OkHttpClient client, List<Class> kClasses) throws Exception {
        IVocabularyElement response = null;
        for (var type : kClasses) {
            response = resolveMasterDataItem(type, options, relativeURL, client);
            if (response != null) break;
        }
        return response;
    }

    public static IVocabularyElement resolveMasterDataItem(Class type, DigitalLinkQueryOptions options, String relativeURL, OkHttpClient client) throws Exception {
        if (options.url == null) {
            throw new Exception("options.Uri is null on the DigitalLinkQueryOptions");
        }

        Request request = new Request.Builder()
                .url(new URI(URLHelper.Combine(options.url.toString(), relativeURL)).toURL())
                .get()
                .build();

        Response response = client.newCall(request).execute();
        String responseStr = response.body().string();

        if (response.code() == 200) {
            ObjectMapper objectMapper = new ObjectMapper();
            DigitalLink[] links = objectMapper.readValue(responseStr, DigitalLink[].class);
            for (DigitalLink link : links) {
                try {
                    Request.Builder secondRequest = new Request.Builder().url(new URI(link.link).toURL()).get();
                    Response itemResponse = client.newCall(secondRequest.build()).execute();
                    String itemResponseStr = itemResponse.body().string();

                    if (itemResponse.code() == 200) {
                        IVocabularyElement item = OpenTraceabilityMappers.MasterData.GS1WebVocab.map(type, itemResponseStr);
                        if (item != null) {
                            if (item.id == null) {
                                throw new Exception("While resolve a " + type + " through the GS1 Digital Link Resolver, the " + type + " returned " +
                                        "had an empty or invalid Identifier. The link that was resolved was " + link + " and the results was " + itemResponseStr);
                            } else {
                                return item;
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        } else {
            throw new Exception("There was an error trying to fetch digital links from " + relativeURL + " - " + response + " - " + responseStr);
        }

        return null;
    }
}