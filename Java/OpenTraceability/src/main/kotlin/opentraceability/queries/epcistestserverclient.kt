package queries

import interfaces.IEPCISDocumentMapper
import mappers.*
import models.events.*
import models.identifiers.EPC
import okhttp3.*
import java.net.URL
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import utility.HttpClientPool
import java.net.URI
import java.util.*

class EPCISTestServerClient(val _baseURL: String, val _format: EPCISDataFormat, val _version: EPCISVersion) {

    private val client = OkHttpClient()


    suspend fun postEPCISDocument(doc: EPCISDocument, blobId: String? = null): String = withContext(Dispatchers.IO) {
        val baseUrl = _baseURL.trimEnd('/')
        val actualBlobId = blobId ?: UUID.randomUUID().toString()
        val url = "$baseUrl/epcis/$actualBlobId/events"

        val mapper: IEPCISDocumentMapper = if (_format == EPCISDataFormat.JSON) {
            OpenTraceabilityMappers.EPCISDocument.JSON
        } else {
            OpenTraceabilityMappers.EPCISDocument.XML
        }

        val contentType = if (_format == EPCISDataFormat.JSON) {
            "application/json"
        } else {
            "application/xml"
        }

        val clientItem = HttpClientPool.getClient()
        val client = clientItem.value

        val request = Request.Builder()
            .url(url)
            .header("Accept", if (_format == EPCISDataFormat.XML) "application/xml" else "application/json")
            .header("GS1-EPCIS-Version", if (_version == EPCISVersion.V1) "1.2" else "2.0")
            .header("GS1-EPCIS-Min", if (_version == EPCISVersion.V1) "1.2" else "2.0")
            .header("GS1-EPCIS-Max", if (_version == EPCISVersion.V1) "1.2" else "2.0")
            .header("GS1-CBV-Version", if (_version == EPCISVersion.V1) "1.2" else "2.0")
            .header("GS1-CBV-XML-Format", "ALWAYS_URN")
            .post(RequestBody.create(contentType.toMediaTypeOrNull(), mapper.Map(doc)))
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            val contentStr = response.body?.string()
            throw Exception("${response.code} - ${response.message} - $contentStr")
        }

        actualBlobId
    }


    suspend fun queryEvents(blobId: String, parameters: EPCISQueryParameters): EPCISQueryResults {
        val url = "${_baseURL.trimEnd('/')}/epcis/$blobId"

        val options = EPCISQueryInterfaceOptions().apply {
            this.URL = URL(url).toURI()
            this.Format = _format
            this.Version = _version
            this.EnableStackTrace = true
        }

        return EPCISTraceabilityResolver.queryEvents(options, parameters, client)
    }

    suspend fun traceback(blobId: String, epc: EPC): EPCISQueryResults {
        val url = "${_baseURL.trimEnd('/')}/epcis/$blobId"

        val options = EPCISQueryInterfaceOptions().apply {
            this.URL = URL(url).toURI()
            this.Format = _format
            this.Version = _version
            this.EnableStackTrace = true
        }

        return EPCISTraceabilityResolver.traceback(options, epc, client)
    }

    suspend fun resolveMasterData(blobId: String, doc: EPCISBaseDocument) {
        val url = "${_baseURL.trimEnd('/')}/digitallink/$blobId"

        val options = DigitalLinkQueryOptions().apply {
            this.URL = URL(url).toURI()
            this.EnableStackTrace = true
        }

        MasterDataResolver.resolveMasterData(options, doc, client)
    }
}

