package opentraceability.queries

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import opentraceability.interfaces.IVocabularyElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import opentraceability.models.identifiers.*
import opentraceability.models.events.*
import opentraceability.models.masterdata.*
import opentraceability.models.events.EPCISBaseDocument
import opentraceability.models.identifiers.GLN
import opentraceability.models.identifiers.GTIN
import opentraceability.models.identifiers.PGLN
import opentraceability.models.masterdata.TradingParty
import opentraceability.mappers.OpenTraceabilityMappers
import opentraceability.models.masterdata.DigitalLink
import java.lang.reflect.Type
import okhttp3.OkHttpClient
import java.net.URI
import okhttp3.Request
import okhttp3.Response
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf


class MasterDataResolver {
    companion object {
        suspend fun resolveMasterData(
            options: DigitalLinkQueryOptions,
            doc: EPCISBaseDocument,
            client: OkHttpClient
        ) {
            for (evt in doc.events) {
                for (p in evt.products) {
                    if (p.EPC.Type == EPCType.Class || p.EPC.Type == EPCType.Instance) {
                        resolveTradeitem(options, p.EPC.GTIN, doc, client)
                    }
                }

                resolveLocation(options, evt.location?.gln, doc, client)

                for (source in evt.sourceList) {
                    if (source.ParsedType == EventSourceType.Owner) {
                        val pgln = PGLN(source.Value ?: throw Exception("source in event source list has NULL value."))
                        if (pgln != null) {
                            resolveTradingParty(options, pgln, doc, client)
                        }
                    }
                }

                for (dest in evt.destinationList) {
                    if (dest.ParsedType == EventDestinationType.Owner) {
                        val pgln = PGLN(dest.value ?: throw Exception("source in event source list has NULL value."))
                        if (pgln != null) {
                            resolveTradingParty(options, pgln, doc, client)
                        }
                    }
                }
            }
        }

        suspend fun resolveTradeitem(
            options: DigitalLinkQueryOptions,
            gtin: GTIN?,
            doc: EPCISBaseDocument,
            client: OkHttpClient
        ) {
            if (gtin != null) {
                if (doc.searchMasterData<TradeItem>(gtin.toString()) == null) {
                    val t = opentraceability.Setup.getMasterDataTypeDefault(typeOf<TradeItem>()) ?: throw Exception("failed to find master data type for Trade Item")
                    val ti = resolveMasterDataItem(t, options, "/01/$gtin?linkType=gs1:masterData", client)
                    if (ti != null) {
                        doc.masterData.add(ti)
                    }
                }
            }
        }

        suspend fun resolveLocation(
            options: DigitalLinkQueryOptions,
            gln: GLN?,
            doc: EPCISBaseDocument,
            client: OkHttpClient
        ) {
            if (gln != null) {
                if (doc.searchMasterData<Location>(gln.toString()) == null) {
                    val t = opentraceability.Setup.getMasterDataTypeDefault(typeOf<Location>()) ?: throw Exception("failed to find master data type for Location")
                    val l = resolveMasterDataItem(t, options, "/414/$gln?linkType=gs1:masterData", client)
                    if (l != null) {
                        doc.masterData.add(l)
                    }
                }
            }
        }

        suspend fun resolveTradingParty(
            options: DigitalLinkQueryOptions,
            pgln: PGLN?,
            doc: EPCISBaseDocument,
            client: OkHttpClient
        ) {
            if (pgln != null) {
                if (doc.searchMasterData<TradingParty>(pgln.toString()) == null) {
                    val t = opentraceability.Setup.getMasterDataTypeDefault(typeOf<TradingParty>()) ?: throw Exception("failed to find master data type for Trading Party")
                    val tp = resolveMasterDataItem(t, options, "/417/$pgln?linkType=gs1:masterData", client)
                    if (tp != null) {
                        doc.masterData.add(tp)
                    }
                }
            }
        }

        suspend inline fun <reified T : IVocabularyElement> resolverMasterDataItem(
            options: DigitalLinkQueryOptions,
            relativeURL: String,
            client: OkHttpClient
        ): T? {
            val response = resolveMasterDataItem(typeOf<T>() as KClass<IVocabularyElement>, options, relativeURL, client)
            return response as? T
        }

        suspend fun resolveMasterDataItem(
            type: KClass<IVocabularyElement>,
            options: DigitalLinkQueryOptions,
            relativeURL: String,
            client: OkHttpClient
        ): IVocabularyElement? {
            if (options.URL == null)
            {
                throw Exception("options.Uri is null on the DigitalLinkQueryOptions")
            }

            val client = OkHttpClient()

            val request = Request.Builder()
                .url(URI(options.URL.toString().trimEnd('/') + "/" + relativeURL.trimStart('/')).toURL())
                .get()
                .build()

            val response: Response = client.newCall(request).execute()
            val responseStr = response.body?.string()

            if (response.code == 200) {
                val objectMapper = ObjectMapper()
                val links = objectMapper.readValue(responseStr, Array<DigitalLink>::class.java)
                if (links.isNotEmpty()) {
                    for (link in links) {
                        try {
                            var secondRequest = Request.Builder().url(URI(link.link).toURL()).get().build()

                            val itemResponse: Response = client.newCall(request).execute()
                            val itemResponseStr = response.body?.string() ?: ""

                            if (itemResponse.code == 200) {
                                val item = OpenTraceabilityMappers.MasterData.GS1WebVocab.map(type, itemResponseStr)
                                if (item != null) {
                                    if (item.id == null)
                                    {
                                        throw Exception("While resolve a $type through the GS1 Digital Link Resolver, the $type returned " +
                                                "had an empty or invalid Identifier. The link that was resolved was $link and the results was ${itemResponseStr}")
                                    } else {
                                        return item
                                    }
                                }
                            }
                        } catch (ex: Exception) {
                            println(ex)
                        }
                    }
                }
            } else {
                throw Exception("There was an error trying to fetch digital links from $relativeURL - $response - $responseStr")
            }

            return null
        }
    }
}
