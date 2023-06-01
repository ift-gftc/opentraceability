package models.events

import utility.*
import utility.attributes.*
import java.net.URI

class EventSource {

    @OpenTraceabilityJsonAttribute("type")
    @OpenTraceabilityAttribute("", "@type")
    var Type: URI? = null


    @OpenTraceabilityJsonAttribute("source")
    @OpenTraceabilityAttribute("", "text()")
    var Value: String? = null

    val ParsedType: EventSourceType
        get() {
            var type = EventSourceType.Unknown

            for (e in enumValues<EventSourceType>()) {
                if (EnumUtil.GetEnumAttributes<CBVAttribute>(e)
                        .any { it.value.toLowerCase() == type?.toString()?.toLowerCase() }
                ) {
                    return e
                }
            }

            return type
        }
        set(value) {
            val t = EnumUtil.GetEnumAttributes<CBVAttribute>(value).firstOrNull { it.value.startsWith("urn") }?.value
            if (!t.isNullOrBlank()) {
                this.Type = URI(t)
            }
        }

}
