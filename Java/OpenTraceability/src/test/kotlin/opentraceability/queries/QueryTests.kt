package opentraceability.queries

import kotlinx.coroutines.runBlocking
import opentraceability.OpenTraceabilityTests
import opentraceability.gdst.MasterData.GDSTLocation
import opentraceability.mappers.OpenTraceabilityMappers
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.IOException
import java.net.*
import java.security.cert.X509Certificate
import java.time.OffsetDateTime
import javax.net.ssl.*
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.*

class QueryTests {

    companion object {
        //var _testServer: IWebHost? = null
        var _config: Properties? = null

        init {

            try {


                _config = OpenTraceabilityTests.getConfiguration("appsettings.TestServer")
                //_testServer = testServer.WebServiceFactory.create("https://localhost:4001", config)
            } catch (e: Exception) {
                e.printStackTrace()
                fail("Error occurred while getting config")
            }


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
        parameters.query.EQ_bizLocation = mutableListOf(URI("urn:epc:id:sgln:0614141.00888.0"), URI("urn:epc:id:sgln:0614141.00888.0"))
        parameters.query.EQ_bizStep = mutableListOf("https://ref.gs1.org/cbv/BizStep-shipping", "receiving")
        parameters.query.GE_eventTime = OffsetDateTime.now()
        parameters.query.GE_recordTime = OffsetDateTime.now()
        parameters.query.LE_eventTime = OffsetDateTime.now()
        parameters.query.LE_recordTime = OffsetDateTime.now()


        val queryParameters = parameters.toQueryParameters()
        val uri = URI("https://example.org$queryParameters")

        val paramsAfter = EPCISQueryParameters(uri)

        opentraceability.OpenTraceabilityTests.compareJSON(parameters.toJSON(), paramsAfter.toJSON())
    }

    @Test
    fun queryParameters02() {
        val parameters = EPCISQueryParameters()
        parameters.query.MATCH_epc = mutableListOf("https://id.gs1.org/01/00614141777778/10/987")
        parameters.query.MATCH_anyEPC =
            mutableListOf("https://id.gs1.org/01/00614141777778/10/987", "https://id.gs1.org/01/00614141777778/10/987")
        parameters.query.MATCH_epcClass = mutableListOf("urn:epc:class:lgtin:4012345.012345.998877")
        parameters.query.MATCH_anyEPCClass =
            mutableListOf("urn:epc:class:lgtin:4012345.012345.998877", "urn:epc:class:lgtin:4012345.012345.998877")
        parameters.query.GE_eventTime = OffsetDateTime.now()
        parameters.query.GE_recordTime = OffsetDateTime.now()
        parameters.query.LE_eventTime = OffsetDateTime.now()
        parameters.query.LE_recordTime = OffsetDateTime.now()
        parameters.query.EQ_bizStep = mutableListOf("https://ref.gs1.org/cbv/BizStep-shipping", "receiving")

        val queryParameters = parameters.toQueryParameters()
        val uri = URI("https://example.org$queryParameters")

        val paramsAfter = EPCISQueryParameters(uri)

        opentraceability.OpenTraceabilityTests.compareJSON(parameters.toJSON(), paramsAfter.toJSON())
    }

    @Test
    suspend fun masterData() {
        val client = EPCISTestServerClient(
            "https://localhost:4001",
            opentraceability.mappers.EPCISDataFormat.JSON,
            opentraceability.models.events.EPCISVersion.V2
        )

        val filename = "testserver_advancedfilters.jsonld"
        val data = OpenTraceabilityTests.readTestData(filename)
        val doc = OpenTraceabilityMappers.EPCISDocument.JSON.map(data)
        val blobId = client.postEPCISDocument(doc)

        var foundOneGDSTLocation = false

        runBlocking {
            doc.events.forEach { e ->
                e.products.forEach { p ->
                    val parameters = EPCISQueryParameters(p.EPC!!)
                    val results = client.queryEvents(blobId, parameters)
                    assertNotNull(results.Document)
                    assertEquals(0, results.Errors.size, "Errors found in the query events")
                    assertNotEquals(0, results.Document?.events?.size, "No events returned")

                    results.Document?.let { client.resolveMasterData(blobId, it) }
                    assertNotEquals(0, results.Document?.masterData?.size, "No master data resolved")

                    if (results.Document?.masterData?.any { it is GDSTLocation } == true) {
                        foundOneGDSTLocation = true
                    }
                }
            }
        }

        assertTrue(foundOneGDSTLocation, "Did not find GDSTLocation.")
    }

    @Test
    suspend fun getEPCISQueryInterfaceURL(filename: String) {

        val client = createInsecureHttpClient()
        val epcisTestServerClient = EPCISTestServerClient(
            "https://localhost:4001",
            opentraceability.mappers.EPCISDataFormat.JSON,
            opentraceability.models.events.EPCISVersion.V2
        )


        // upload a blob of events
        val data = OpenTraceabilityTests.readTestData(filename)
        val doc = OpenTraceabilityMappers.EPCISDocument.JSON.map(data)
        val blobId = epcisTestServerClient.postEPCISDocument(doc)

        val queryOptions = DigitalLinkQueryOptions()
        queryOptions.URL = URI("https://localhost:4001/digitallink/$blobId/")


        // grab the traceability data...
        for (event in doc.events) {
            for (product in event.products) {
                val request = HttpRequest.newBuilder()
                    .uri(queryOptions.URL)
                    .build()

                val epcisQueryInterfaceURL = client.send(request, HttpResponse.BodyHandlers.ofString())
                assertNotNull(epcisQueryInterfaceURL, "Failed to get EPCIS URL for ${product.EPC}")
            }
        }
    }


    @Test
    @Throws(IOException::class)
    suspend fun queryEvents(filename: String) {
        val client = EPCISTestServerClient(
            "https://localhost:4001",
            opentraceability.mappers.EPCISDataFormat.JSON,
            opentraceability.models.events.EPCISVersion.V2
        )
// upload a blob of events
        val data = OpenTraceabilityTests.readTestData(filename)
        val doc = OpenTraceabilityMappers.EPCISDocument.JSON.map(data)
        val blobId = client.postEPCISDocument(doc)

// query for the events for each epc in the blob
        for (event in doc.events) {
            for (product in event.products) {
                val parameters = EPCISQueryParameters(product.EPC!!)
                val results = client.queryEvents(blobId, parameters)
                assertEquals(0, results.Errors.size, "errors found in the query events")
                assertEquals(1, results.Document?.events?.size, "no events returned")
            }
        }
    }


    @Test
    @Throws(IOException::class)
    suspend fun advancedFilters(filename: String, epc: String, bizStep: String, bizLocation: String) {
        val client = EPCISTestServerClient(
            "https://localhost:4001",
            opentraceability.mappers.EPCISDataFormat.JSON,
            opentraceability.models.events.EPCISVersion.V2
        )
        // upload a blob of events
        val data = OpenTraceabilityTests.readTestData(filename)
        val doc = OpenTraceabilityMappers.EPCISDocument.JSON.map(data)
        val blobId = client.postEPCISDocument(doc)

// query for the events for each epc in the blob
        val parameters = EPCISQueryParameters(opentraceability.models.identifiers.EPC(epc))
        parameters.query.EQ_bizStep = mutableListOf(bizStep)
        parameters.query.EQ_bizLocation = mutableListOf(URI(bizLocation))

        val results = client.queryEvents(blobId, parameters)
        assertEquals(0, results.Errors.size, "errors found in the query events")
        assertEquals(1, results.Document?.events?.size, "no events returned")
    }

    @Test
    suspend fun traceback(filename: String) {
        val client = EPCISTestServerClient(
            "https://localhost:4001",
            opentraceability.mappers.EPCISDataFormat.JSON,
            opentraceability.models.events.EPCISVersion.V2
        )

        // upload a blob of events
        val data = OpenTraceabilityTests.readTestData(filename)
        val doc = OpenTraceabilityMappers.EPCISDocument.JSON.map(data)
        val blobId = client.postEPCISDocument(doc)

        val results = client.traceback(
            blobId,
            opentraceability.models.identifiers.EPC("urn:gdst:example.org:product:lot:class:processor.2u.v1-0122-2022")
        )
        assertEquals(0, results.Errors.size, "errors found in the traceback events")
        assertNotNull(results.Document)
        assertEquals(16, results.Document?.events?.size, "expected 16 events")
    }


    @Throws(NoSuchAlgorithmException::class, KeyManagementException::class)
    fun createInsecureHttpClient(): HttpClient {
        val trustManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
        }

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf<TrustManager>(trustManager), SecureRandom())

        return HttpClient.newBuilder()
            .sslContext(sslContext)
            .build()
    }


}