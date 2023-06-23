package opentraceability.models.events

import opentraceability.utility.attributes.*
import java.net.URI

class PersistentDisposition {

    @OpenTraceabilityAttribute("","unset", 1)
    @OpenTraceabilityArrayAttribute
    var unset: MutableList<URI>? = null

    @OpenTraceabilityAttribute("","set", 2)
    @OpenTraceabilityArrayAttribute
    var set: MutableList<URI>? = null
}
