package queries

import java.net.http.HttpClient
import models.identifiers.*
import models.identifiers.EPC
import models.identifiers.PGLN
import java.net.URI

class EPCISTraceabilityResolver {
    companion object {
        fun GetEPCISQueryInterfaceURL(options: DigitalLinkQueryOptions, epc: EPC, client: HttpClient): URI? {
            TODO("Not yet implemented")
        }

        fun GetEPCISQueryInterfaceURL(options: DigitalLinkQueryOptions, pgln: PGLN, client: HttpClient): URI? {
            TODO("Not yet implemented")
        }

        fun Traceback(
            options: EPCISQueryInterfaceOptions,
            epc: EPC,
            client: HttpClient,
            additionalParameters: EPCISQueryParameters
        ): EPCISQueryResults {
            TODO("Not yet implemented")
        }


        fun QueryEvents(
            options: EPCISQueryInterfaceOptions,
            parameters: EPCISQueryParameters,
            client: HttpClient
        ): EPCISQueryResults {
            TODO("Not yet implemented")
        }
    }
}
