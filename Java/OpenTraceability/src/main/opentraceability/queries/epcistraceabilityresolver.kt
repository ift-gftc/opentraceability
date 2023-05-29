package opentraceability.queries

import java.net.http.HttpClient
import opentraceability.models.identifiers.*
import opentraceability.models.identifiers.EPC
import opentraceability.models.identifiers.PGLN
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
