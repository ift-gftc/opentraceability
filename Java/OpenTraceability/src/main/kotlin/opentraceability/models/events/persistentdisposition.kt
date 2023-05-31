package models.events

import utility.attributes.*
import java.net.URI

class PersistentDisposition {

    @OpenTraceabilityAttribute("","unset", 1)
    @OpenTraceabilityArrayAttribute
    var Unset: ArrayList<URI>? = null

    @OpenTraceabilityAttribute("","set", 2)
    @OpenTraceabilityArrayAttribute
    var Set: ArrayList<URI>? = null
}
