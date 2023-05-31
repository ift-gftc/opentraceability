package models.events

import utility.attributes.OpenTraceabilityAttribute
import java.net.URI

class EventReadPoint {

    @OpenTraceabilityAttribute("","id")
    var ID: URI? = null
}
