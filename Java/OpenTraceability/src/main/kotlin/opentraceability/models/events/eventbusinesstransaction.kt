package opentraceability.models.events

import opentraceability.utility.attributes.*
import java.net.URI

class EventBusinessTransaction {

    @OpenTraceabilityJsonAttribute("type")
    @OpenTraceabilityAttribute("", "@type")
    var type: URI? = null

    @OpenTraceabilityJsonAttribute("bizTransaction")
    @OpenTraceabilityAttribute("", "text()")
    var value: String? = null

}
