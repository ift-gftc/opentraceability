package opentraceability.queries

import java.util.*
import opentraceability.models.identifiers.*
import java.net.URI
import java.time.LocalDateTime

class EPCISQueryStackTraceItem {

    var ID: String = UUID.randomUUID().toString()
    var Created: LocalDateTime = LocalDateTime.now()
    //var ResponseStatusCode: HttpStatusCode? = null
    var ResponseStatusCode: Int? = null
    var RelativeURL: URI? = null
    var RequestHeaders: MutableList<MutableMap<String, MutableList<String>>>? = null
    var ResponseHeaders: MutableList<MutableMap<String, MutableList<String>>>? = null
    var RequestBody: String? = null
    var ResponseBody: String? = null

}
