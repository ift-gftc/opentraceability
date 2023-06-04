package opentraceability.models.events

import opentraceability.utility.CBVAttribute
import opentraceability.utility.EnumUtil
import opentraceability.utility.attributes.*
import java.net.URI

class EventDestination {

    @OpenTraceabilityJsonAttribute("type")
    @OpenTraceabilityAttribute("","@type")
    var type: URI? = null

    @OpenTraceabilityJsonAttribute("destination")
    @OpenTraceabilityAttribute("","text()")
    var value: String? = null

    var ParsedType: EventDestinationType
        get() {
            var type = EventDestinationType.Unknown

            for (e in enumValues<EventDestinationType>()) {
                if (EnumUtil.GetEnumAttributes<EventDestinationType, CBVAttribute>(e)
                        .any { it.value.toLowerCase() == type?.toString()?.toLowerCase() }
                ) {
                    return e
                }
            }

            return type
        }
        set(value) {
            val t = EnumUtil.GetEnumAttributes<EventDestinationType, CBVAttribute>(value).firstOrNull { it.value.startsWith("urn") }?.value
            if (!t.isNullOrBlank()) {
                this.type = URI(t)
            }
        }

}
