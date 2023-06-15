package opentraceability.queries;

import com.fasterxml.jackson.databind.ObjectMapper;
import opentraceability.interfaces.IEvent;
import opentraceability.interfaces.IVocabularyElement;
import opentraceability.mappers.OpenTraceabilityMappers;
import opentraceability.models.events.EPCISBaseDocument;
import opentraceability.models.events.EventDestinationType;
import opentraceability.models.events.EventProduct;
import opentraceability.models.events.EventSourceType;
import opentraceability.models.masterdata.Location;
import opentraceability.models.masterdata.TradeItem;
import opentraceability.models.masterdata.TradingParty;
import opentraceability.models.identifiers.*;
import java.net.URI;

public class MasterDataResolver {
    public static void resolveMasterData(DigitalLinkQueryOptions options, EPCISBaseDocument doc, OkHttpClient client) throws Exception {
        for (IEvent evt : doc.events) {
            for (EventProduct p : evt.products) {
                if (p.EPC.Type == EPCType.Class || p.EPC.Type == EPCType.Instance) {
                    resolveTradeitem(options, p.EPC.GTIN, doc, client);
                }
            }

            resolveLocation(options, evt.location.gln, doc, client);

            for (EPCISBaseExtension source : evt.sourceList) {
                if (source.ParsedType == EventSourceType.Owner) {
                    PGLN pgln = new PGLN(source.Value);
                    if (pgln != null) {
                        resolveTradingParty(options, pgln, doc, client);
                    }
                }
            }

            for (EPCISBaseExtension dest : evt.destinationList) {
                if (dest.ParsedType == EventDestinationType.Owner) {
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
            if (doc.searchMasterData(gtin.toString()) == null) {
                KClass<TradeItem> type = typeOf(TradeItem.class);
                TradeItem t = opentraceability.Setup.getMasterDataTypeDefault(type) != null ? throw new Exception("failed to find master data type for Trade Item") : null;
                TradeItem ti = resolveMasterDataItem(t, options, "/01/" + gtin.toString() + "?linkType=gs1:masterData", client);
                if (ti != null) {
                    doc.masterData.add(ti);
                }
            }
        }
    }

    public static void resolveLocation(DigitalLinkQueryOptions options, GLN gln, EPCISBaseDocument doc, OkHttpClient client) throws Exception {
        if (gln != null) {
            if (doc.searchMasterData(gln.toString()) == null) {
                KClass<Location> type = typeOf(Location.class);
                Location t = opentraceability.Setup.getMasterDataTypeDefault(type) != null ? throw new Exception("failed to find master data type for Location") : null;
                Location l = resolveMasterDataItem(t, options, "/414/" + gln.toString() + "?linkType=gs1:masterData", client);
                if (l != null) {
                    doc.masterData.add(l);
                }
            }
        }
    }

    public static void resolveTradingParty(DigitalLinkQueryOptions options, PGLN pgln, EPCISBaseDocument doc, OkHttpClient client) throws Exception {
        if (pgln != null) {
            if (doc.searchMasterData(pgln.toString()) == null) {
                KClass<TradingParty> type = typeOf(TradingParty.class);
                TradingParty t = opentraceability.Setup.getMasterDataTypeDefault(type) != null ? throw new Exception("failed to find master data type for Trading Party") : null;
                TradingParty tp = resolveMasterDataItem(t, options, "/417/" + pgln.toString() + "?linkType=gs1:masterData", client);
                if (tp != null) {
                    doc.masterData.add(tp);
                }
            }
        }
    }

    public static IVocabularyElement resolverMasterDataItem(DigitalLinkQueryOptions options, String relativeURL, OkHttpClient client, List<KClass<IVocabularyElement>> kClasses) throws Exception {
        IVocabularyElement response = null;
        for (KClass<IVocabularyElement> clazz : kClasses) {
            response = resolveMasterDataItem(clazz, options, relativeURL, client);
            if (response != null) break;
        }
        return response;
    }

    public static IVocabularyElement resolveMasterDataItem(KClass<IVocabularyElement> type, DigitalLinkQueryOptions options, String relativeURL, OkHttpClient client) throws Exception {
        if (options.URL == null) {
            throw new Exception("options.Uri is null on the DigitalLinkQueryOptions");
        }

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(new URI(options.URL.toString().trim() + "/" + relativeURL.trimStart("/")).toURL())
                .get()
                .build();

        Response response = client.newCall(request).execute();
        String responseStr = response.body().string();

        if (response.code() == 200) {
            ObjectMapper objectMapper = new ObjectMapper();
            DigitalLink[] links = objectMapper.readValue(responseStr, DigitalLink[].class);
            if (links.length > 0) {
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
            }
        } else {
            throw new Exception("There was an error trying to fetch digital links from " + relativeURL + " - " + response + " - " + responseStr);
        }

        return null;
    }
}