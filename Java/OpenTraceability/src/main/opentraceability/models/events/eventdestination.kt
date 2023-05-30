package models.events

import utility.CBVAttribute
import utility.attributes.OpenTraceabilityAttribute
import utility.attributes.OpenTraceabilityJsonAttribute
import java.lang.reflect.Type
import java.net.URI

class EventDestination {

    @OpenTraceabilityJsonAttribute("type")
    @OpenTraceabilityAttribute("","@type")
    var Type: URI? = null

    @OpenTraceabilityJsonAttribute("destination")
    @OpenTraceabilityAttribute("","text()")
    var Value: String? = null

    var parsedType: EventDestinationType
        get() {
            var type = EventDestinationType.Unknown

            for (e in EventDestinationType.values()) {
                val annotation = e::class.java.getAnnotation(CBVAttribute::class.java)
                if (annotation != null && annotation.Value.equals(type.toString(), ignoreCase = true)) {
                    return e
                }
            }

            return type
        }
        set(value) {
            val annotation = value::class.java.getAnnotation(CBVAttribute::class.java)
            if (annotation != null && annotation.Value.isNotBlank() && annotation.Value.startsWith("urn")) {
                this.Type = URI.create(annotation.Value)
            }
        }

}
