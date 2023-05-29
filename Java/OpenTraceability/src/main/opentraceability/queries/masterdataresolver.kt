package queries

import io.grpc.netty.shaded.io.netty.internal.tcnative.AsyncTask
import java.net.http.HttpClient
import models.identifiers.*
import models.events.*
import models.masterdata.Location
import models.masterdata.Tradeitem
import models.events.EPCISBaseDocument
import models.identifiers.GLN
import models.identifiers.GTIN
import models.identifiers.PGLN
import models.masterdata.TradingParty
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
