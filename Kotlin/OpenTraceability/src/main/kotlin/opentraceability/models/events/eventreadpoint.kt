package opentraceability.models.events

import opentraceability.utility.attributes.OpenTraceabilityAttribute
import java.net.URI

class EventReadPoint {

    @OpenTraceabilityAttribute("","id")
    var ID: URI? = null
}
