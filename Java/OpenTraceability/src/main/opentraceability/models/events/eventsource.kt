package models.events
import java.lang.reflect.Type
import java.net.URI
class EventSource {
    var Type: URI? = URI?()
    var ParsedType: EventSourceType = EventSourceType()
    var Value: String = String()
    companion object{
    }
}
