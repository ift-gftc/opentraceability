package models.events
import utility.attributes.OpenTraceabilityAttribute
import utility.attributes.OpenTraceabilityJsonAttribute
import java.lang.reflect.Type
import java.net.URI
class EventBusinessTransaction {

    @OpenTraceabilityJsonAttribute("type")
    @OpenTraceabilityAttribute("","@type")
    var Type: URI? = null

    @OpenTraceabilityJsonAttribute("bizTransaction")
    @OpenTraceabilityAttribute("","text()")
    var Value: String? = null

}
