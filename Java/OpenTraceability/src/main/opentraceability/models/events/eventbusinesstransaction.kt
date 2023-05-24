package models.events
import java.lang.reflect.Type
import java.net.URI
class EventBusinessTransaction {

    //TODO: review this

    //[OpenTraceabilityJson("type")]
    //[OpenTraceability("@type")]
    var Type: URI? = null

    //[OpenTraceabilityJson("bizTransaction")]
    //[OpenTraceability("text()")]
    var Value: String? = null

}
