package opentraceability
import opentraceability.interfaces.IEvent
import opentraceability.models.events.EventAction
import opentraceability.models.events.EventType
import java.util.*
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.KType

class OpenTraceabilityEventProfile {

    var SpecificityScore: Int = 0
        get() {
            var score: Int = 1
            if (Action != null){
                score ++
            }
            if (BusinessStep != null){
                score ++
            }
            if (KDEProfiles != null){
                score += KDEProfiles!!.count()
            }
            return field
        }

    lateinit var EventType: EventType
    var Action: EventAction? = null
    var BusinessStep: String = ""
    lateinit var EventClassType: KClass<IEvent>
    var KDEProfiles: MutableList<OpenTraceabilityEventKDEProfile>? = null

    constructor(){}

    constructor(eventClassType: KClass<IEvent>, eventType: EventType) {
        EventType = eventType;
        EventClassType = eventClassType;
    }

    constructor(eventClassType: KClass<IEvent>, eventType: EventType, businessStep: String) {
        EventType = eventType;
        EventClassType = eventClassType;
        BusinessStep = businessStep;
    }

    constructor(eventClassType: KClass<IEvent>, eventType: EventType, action: EventAction) {
        EventType = eventType;
        EventClassType = eventClassType;
        Action = action;
    }

    constructor(eventClassType: KClass<IEvent>, eventType: EventType, businessStep: String, action: EventAction) {
        EventType = eventType;
        EventClassType = eventClassType;
        Action = action;
        BusinessStep = businessStep;
    }

    constructor(eventClassType: KClass<IEvent>, eventType: EventType, kdeProfiles: MutableList<OpenTraceabilityEventKDEProfile>) {
        KDEProfiles = kdeProfiles;
        EventType = eventType;
        EventClassType = eventClassType;
    }

    constructor(eventClassType: KClass<IEvent>, eventType: EventType, businessStep: String, kdeProfiles: MutableList<OpenTraceabilityEventKDEProfile>) {
        KDEProfiles = kdeProfiles;
        EventType = eventType;
        EventClassType = eventClassType;
        BusinessStep = businessStep;
    }

    constructor(eventClassType: KClass<IEvent>, eventType: EventType, action: EventAction, kdeProfiles: MutableList<OpenTraceabilityEventKDEProfile>) {
        KDEProfiles = kdeProfiles;
        EventType = eventType;
        EventClassType = eventClassType;
        Action = action;
    }

    constructor(eventClassType: KClass<IEvent>, eventType: EventType, businessStep: String, action: EventAction, kdeProfiles: MutableList<OpenTraceabilityEventKDEProfile>) {
        KDEProfiles = kdeProfiles;
        EventType = eventType;
        EventClassType = eventClassType;
        Action = action;
        BusinessStep = businessStep;
    }

    override fun toString(): String {

        var eventTypes = "";

        if (KDEProfiles != null && KDEProfiles!!.count() > 0){
            eventTypes = KDEProfiles!!.joinToString (separator = ":") { p -> "\'${p}\'" }
        }

        return "$EventType:$Action:$BusinessStep:$eventTypes"
    }

}
