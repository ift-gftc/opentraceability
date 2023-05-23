import models.events.EventAction
import models.events.EventType
import java.util.*
import java.lang.reflect.Type
class OpenTraceabilityEventProfile {
    var SpecificityScore: Int = Int()
    var EventType: EventType = EventType()
    var Action: EventAction? = null
    var BusinessStep: String = String()
    var EventClassType: Type = Type()
    var KDEProfiles: List<OpenTraceabilityEventKDEProfile> = ArrayList<OpenTraceabilityEventKDEProfile>()
    companion object{
    }
}
