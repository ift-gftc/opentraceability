package opentraceability.queries

import opentraceability.mappers.EPCISDataFormat
import opentraceability.models.events.EPCISBaseDocument
import opentraceability.models.events.EPCISDocument
import opentraceability.models.events.EPCISVersion
import opentraceability.models.identifiers.EPC

class EPCISTestServerClient {

    var _baseURL: String = ""
    lateinit var _format: EPCISDataFormat
    lateinit var _version: EPCISVersion

    constructor(baseURL: String, format: EPCISDataFormat, version: EPCISVersion) {
        _baseURL = baseURL;
        _version = version;
        _format = format;
    }

    fun Post(doc: EPCISDocument, blob_id: String? = null): String {
        TODO("Not yet implemented")
    }

    fun QueryEvents(blob_id: String, parameters: EPCISQueryParameters): EPCISQueryResults {
        TODO("Not yet implemented")
    }

    fun Traceback(blob_id: String, epc: EPC): EPCISQueryResults {
        TODO("Not yet implemented")
    }

    fun ResolveMasterData(blob_id: String, doc: EPCISBaseDocument) {
        TODO("Not yet implemented")
    }


}
