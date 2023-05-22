package models.events
import java.lang.reflect.Type
import java.net.URI
class EventDestination {
    var Type: URI? = URI?()
    var ParsedType: EventDestinationType = EventDestinationType()
    var Value: String = String()
    companion object{
    }
}
