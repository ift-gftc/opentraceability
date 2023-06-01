package models.events

import utility.CBVAttribute
import utility.EnumUtil
import utility.attributes.*
import java.net.URI

class EventDestination {

    @OpenTraceabilityJsonAttribute("type")
    @OpenTraceabilityAttribute("","@type")
    var Type: URI? = null

    @OpenTraceabilityJsonAttribute("destination")
    @OpenTraceabilityAttribute("","text()")
    var Value: String? = null

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
                this.Type = URI(t)
            }
        }

}
