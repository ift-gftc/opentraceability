package opentraceability.queries

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.Instant
import java.time.OffsetDateTime
import java.util.*

class QueryTests {

    companion object {
        //var _testServer: IWebHost? = null
        var _config: Properties? = null

        init {
            _config = opentraceability.OpenTraceabilityTests.getConfiguration("appsettings.TestServer")
            //_testServer = opentraceability.testServer.WebServiceFactory.create("https://localhost:4001", config)
        }
    }

    @Test
    fun testServer() {

    }

    @Test
    fun queryParameters() {
        val parameters = EPCISQueryParameters()
        parameters.query.MATCH_epc = mutableListOf("https://id.gs1.org/01/00614141777778/10/987")
        parameters.query.MATCH_anyEPC = mutableListOf("https://id.gs1.org/01/00614141777778/10/987", "https://id.gs1.org/01/00614141777778/10/987")
        parameters.query.MATCH_epcClass = mutableListOf("urn:epc:class:lgtin:4012345.012345.998877")
        parameters.query.MATCH_anyEPCClass = mutableListOf("urn:epc:class:lgtin:4012345.012345.998877", "urn:epc:class:lgtin:4012345.012345.998877")
        parameters.query.GE_eventTime = OffsetDateTime.now()
        parameters.query.GE_recordTime = OffsetDateTime.now()
        parameters.query.LE_eventTime = OffsetDateTime.now()
        parameters.query.LE_recordTime = OffsetDateTime.now()
        parameters.query.EQ_bizLocation = mutableListOf(URI("urn:epc:id:sgln:0614141.00888.0"), URI("urn:epc:id:sgln:0614141.00888.0"))
        parameters.query.EQ_bizStep = mutableListOf("https://ref.gs1.org/cbv/BizStep-shipping", "receiving")

        val queryParameters = parameters.toQueryParameters()
        val uri = URI("https://example.org$queryParameters")

        val paramsAfter = EPCISQueryParameters(uri)

        opentraceability.OpenTraceabilityTests.compareJSON(parameters.toJSON(), paramsAfter.toJSON())
    }

}