package opentraceability.queries

import com.intellij.javaee.web.model.xml.HttpStatusCode
import java.util.*
import models.identifiers.*
import java.net.URI
import java.time.LocalDateTime

class EPCISQueryStackTraceItem {

    var ID: String = UUID.randomUUID().toString()
    var Created: LocalDateTime = LocalDateTime.now()
    var ResponseStatusCode: HttpStatusCode? = null
    var RelativeURL: URI? = null
    var RequestHeaders: ArrayList<MutableMap<String, ArrayList<String>>>? = null
    var ResponseHeaders: ArrayList<MutableMap<String, ArrayList<String>>>? = null
    var RequestBody: String? = null
    var ResponseBody: String? = null

}
