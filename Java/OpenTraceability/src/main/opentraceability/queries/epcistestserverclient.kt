package queries

import mappers.EPCISDataFormat
import models.events.EPCISBaseDocument
import models.events.EPCISDocument
import models.events.EPCISVersion
import models.identifiers.EPC

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mappers.OpenTraceabilityMappers
import java.net.URI
import java.util.*

class EPCISTestServerClient(
    private val baseURL: String,
    private val format: EPCISDataFormat,
    private val version: EPCISVersion
) {
    private val httpClientPool = HttpClientPool()

    suspend fun post(doc: EPCISDocument, blobId: String? = null): String = withContext(Dispatchers.IO) {
        val actualBlobId = blobId ?: UUID.randomUUID().toString()
        val url = "$baseURL/epcis/$actualBlobId/events"

        val mapper = if (format == EPCISDataFormat.JSON) {
            OpenTraceabilityMappers.EPCISDocument.JSON
        } else {
            OpenTraceabilityMappers.EPCISDocument.XML
        }
        val contentType = if (format == EPCISDataFormat.JSON) {
            "application/json"
        } else {
            "application/xml"
        }

        val client = httpClientPool.getClient()

        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", contentType)
            .method("POST", BodyPublishers.ofString(mapper.map(doc)))
            .build()

        val response = client.send(request, BodyHandlers.ofString())
        if (!response.statusCode().isSuccess) {
            val contentStr = response.body()
            throw Exception("${response.statusCode()} - ${response.statusCode()} - $contentStr")
        }

        actualBlobId
    }

    suspend fun queryEvents(blobId: String, parameters: EPCISQueryParameters): EPCISQueryResults =
        withContext(Dispatchers.IO) {
            val client = httpClientPool.getClient()
            val url = "$baseURL/epcis/$blobId"
            val options = EPCISQueryInterfaceOptions(
                URL = URI.create(url),
                Format = format,
                Version = version,
                EnableStackTrace = true
            )

            EPCISTraceabilityResolver.queryEvents(options, parameters, client)
        }

    suspend fun traceback(blobId: String, epc: EPC): EPCISQueryResults = withContext(Dispatchers.IO) {
        val client = httpClientPool.getClient()
        val url = "$baseURL/epcis/$blobId"
        val options = EPCISQueryInterfaceOptions(
            URL = URI.create(url),
            Format = format,
            Version = version,
            EnableStackTrace = true
        )

        EPCISTraceabilityResolver.traceback(options, epc, client)
    }

    suspend fun resolveMasterData(blobId: String, doc: EPCISBaseDocument) = withContext(Dispatchers.IO) {
        val client = httpClientPool.getClient()
        val url = "$baseURL/digitallink/$blobId"
        val options = DigitalLinkQueryOptions(
            URL = URI.create(url),
            EnableStackTrace = true
        )

        MasterDataResolver.resolveMasterData(options, doc, client)
    }
}
