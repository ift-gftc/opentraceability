package models.events

import utility.attributes.*
import java.net.URI

class EventBusinessTransaction {

    @OpenTraceabilityJsonAttribute("type")
    @OpenTraceabilityAttribute("", "@type")
    var Type: URI? = null

    @OpenTraceabilityJsonAttribute("bizTransaction")
    @OpenTraceabilityAttribute("", "text()")
    var Value: String? = null

}
