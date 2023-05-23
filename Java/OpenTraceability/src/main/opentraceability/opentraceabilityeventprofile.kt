import models.events.EventAction
import models.events.EventType
import java.util.*
import java.lang.reflect.Type

class OpenTraceabilityEventProfile {
    var SpecificityScore: Int = Int()
    var EventType: EventType = EventType.ObjectEvent
    var Action: EventAction? = null
    var BusinessStep: String = String()
    var EventClassType: Type = Type()
    var KDEProfiles: List<OpenTraceabilityEventKDEProfile> = ArrayList<OpenTraceabilityEventKDEProfile>()

    constructor(eventClassType: Type, eventType: EventType) {

    }

    constructor(eventClassType: Type, eventType: EventType, businessStep: String) {

    }

    constructor(eventClassType: Type, eventType: EventType, action: EventAction) {

    }

    constructor(eventClassType: Type, eventType: EventType, businessStep: String, action: EventAction) {

    }

    constructor(eventClassType: Type, eventType: EventType, kdeProfiles: List<OpenTraceabilityEventKDEProfile>) {

    }

    constructor(eventClassType: Type, eventType: EventType, businessStep: String, kdeProfiles: List<OpenTraceabilityEventKDEProfile>) {

    }

    constructor(eventClassType: Type, eventType: EventType, action: EventAction, kdeProfiles: List<OpenTraceabilityEventKDEProfile>) {

    }
    constructor(eventClassType: Type, eventType: EventType, businessStep: String,  action: EventAction, kdeProfiles: List<OpenTraceabilityEventKDEProfile>) {

    }

    override fun toString(): String {

        var eventTypes = "";

        if (KDEProfiles != null && KDEProfiles.count() > 0){
            eventTypes = KDEProfiles.joinToString (separator = ":") { p -> "\'${p}\'" }
        }

        return "$EventType:$Action:$BusinessStep:$eventTypes"
    }

}
