package opentraceability.queries

import io.grpc.netty.shaded.io.netty.internal.tcnative.AsyncTask
import java.net.http.HttpClient
import opentraceability.models.identifiers.*
import opentraceability.models.events.*
import opentraceability.models.masterdata.Location
import opentraceability.models.masterdata.Tradeitem
import opentraceability.models.events.EPCISBaseDocument
import opentraceability.models.identifiers.GLN
import opentraceability.models.identifiers.GTIN
import opentraceability.models.identifiers.PGLN
import opentraceability.models.masterdata.TradingParty
import java.lang.reflect.Type

class MasterDataResolver<T> {
    companion object {

        fun ResolveMasterData(options: DigitalLinkQueryOptions, doc: EPCISBaseDocument, client: HttpClient) {
            TODO("Not yet implemented")
        }

        /*
        fun ResolveMasterData(options: DigitalLinkQueryOptions, doc: EPCISBaseDocument, client: HttpClient) {
            TODO("Not yet implemented")
        }
        */

        fun ResolveTradeitem(options: DigitalLinkQueryOptions, gtin: GTIN, doc: EPCISBaseDocument, client: HttpClient) {
            TODO("Not yet implemented")
        }

        /*
        fun ResolveLocation(options: DigitalLinkQueryOptions, gln: GLN, doc: EPCISBaseDocument, client: HttpClient) {
            TODO("Not yet implemented")
        }
        */


        fun ResolveTradingParty(
            options: DigitalLinkQueryOptions,
            pgln: PGLN,
            doc: EPCISBaseDocument,
            client: HttpClient
        ) {
            TODO("Not yet implemented")
        }

        /*
        fun ResolveTradeitem(options: DigitalLinkQueryOptions, gtin: GTIN, doc: EPCISBaseDocument, client: HttpClient) {
            TODO("Not yet implemented")
        }
        */


        fun ResolveLocation(options: DigitalLinkQueryOptions, gln: GLN, doc: EPCISBaseDocument, client: HttpClient) {
            TODO("Not yet implemented")
        }

        /*
        fun ResolveTradingParty(
            options: DigitalLinkQueryOptions,
            pgln: PGLN,
            doc: EPCISBaseDocument,
            client: HttpClient
        ) {
            TODO("Not yet implemented")
        }
        */


        fun ResolveTradeitem(options: DigitalLinkQueryOptions, gtin: GTIN, client: HttpClient): Tradeitem {
            TODO("Not yet implemented")
        }


        fun ResolveLocation(options: DigitalLinkQueryOptions, gln: GLN, client: HttpClient): Location {
            TODO("Not yet implemented")
        }


        fun ResolveTradingParty(options: DigitalLinkQueryOptions, pgln: PGLN, client: HttpClient): TradingParty {
            TODO("Not yet implemented")
        }


        fun <T> ResolverMasterDataItem(options: DigitalLinkQueryOptions, relativeURL: String, client: HttpClient): T {
            TODO("Not yet implemented")
        }

        fun ResolveMasterDataItem(
            type: Type,
            options: DigitalLinkQueryOptions,
            relativeURL: String,
            httpClient: HttpClient
        ): Object {
            TODO("Not yet implemented")
        }
    }
}
