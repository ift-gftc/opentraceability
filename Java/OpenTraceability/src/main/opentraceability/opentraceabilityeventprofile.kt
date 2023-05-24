import models.events.EventAction
import models.events.EventType
import java.util.*
import java.lang.reflect.Type

class OpenTraceabilityEventProfile {
    var SpecificityScore: Int = 0
    lateinit var EventType: EventType
    var Action: EventAction? = null
    var BusinessStep: String = ""
    lateinit var EventClassType: Type
    var KDEProfiles: List<OpenTraceabilityEventKDEProfile>? = ArrayList<OpenTraceabilityEventKDEProfile>()

    constructor(eventClassType: Type, eventType: EventType) {
        EventType = eventType;
        EventClassType = eventClassType;
    }

    constructor(eventClassType: Type, eventType: EventType, businessStep: String) {
        EventType = eventType;
        EventClassType = eventClassType;
        BusinessStep = businessStep;
    }

    constructor(eventClassType: Type, eventType: EventType, action: EventAction) {
        EventType = eventType;
        EventClassType = eventClassType;
        Action = action;
    }

    constructor(eventClassType: Type, eventType: EventType, businessStep: String, action: EventAction) {
        EventType = eventType;
        EventClassType = eventClassType;
        Action = action;
        BusinessStep = businessStep;
    }

    constructor(eventClassType: Type, eventType: EventType, kdeProfiles: List<OpenTraceabilityEventKDEProfile>) {
        KDEProfiles = kdeProfiles;
        EventType = eventType;
        EventClassType = eventClassType;
    }

    constructor(eventClassType: Type, eventType: EventType, businessStep: String, kdeProfiles: List<OpenTraceabilityEventKDEProfile>) {
        KDEProfiles = kdeProfiles;
        EventType = eventType;
        EventClassType = eventClassType;
        BusinessStep = businessStep;
    }

    constructor(eventClassType: Type, eventType: EventType, action: EventAction, kdeProfiles: List<OpenTraceabilityEventKDEProfile>) {
        KDEProfiles = kdeProfiles;
        EventType = eventType;
        EventClassType = eventClassType;
        Action = action;
    }
    constructor(eventClassType: Type, eventType: EventType, businessStep: String,  action: EventAction, kdeProfiles: List<OpenTraceabilityEventKDEProfile>) {
        KDEProfiles = kdeProfiles;
        EventType = eventType;
        EventClassType = eventClassType;
        Action = action;
        BusinessStep = businessStep;
    }

    override fun toString(): String {

        var eventTypes = "";

        if (KDEProfiles != null && KDEProfiles.count() > 0){
            eventTypes = KDEProfiles.joinToString (separator = ":") { p -> "\'${p}\'" }
        }

        return "$EventType:$Action:$BusinessStep:$eventTypes"
    }

}
