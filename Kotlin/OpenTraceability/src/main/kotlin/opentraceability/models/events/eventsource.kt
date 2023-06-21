package opentraceability.models.events

import opentraceability.utility.*
import opentraceability.utility.attributes.*
import java.net.URI

class EventSource {

    @OpenTraceabilityJsonAttribute("type")
    @OpenTraceabilityAttribute("", "@type")
    var Type: URI? = null


    @OpenTraceabilityJsonAttribute("source")
    @OpenTraceabilityAttribute("", "text()")
    var Value: String? = null

    var ParsedType: EventSourceType
        get() {
            var type = EventSourceType.Unknown

            for (e in enumValues<EventSourceType>()) {
                if (EnumUtil.GetEnumAttributes<EventSourceType, CBVAttribute>(e)
                        .any { it.value.toLowerCase() == type?.toString()?.toLowerCase() }
                ) {
                    return e
                }
            }

            return type
        }
        set(value) {
            val t = EnumUtil.GetEnumAttributes<EventSourceType, CBVAttribute>(value).firstOrNull { it.value.startsWith("urn") }?.value
            if (!t.isNullOrBlank()) {
                this.Type = URI(t)
            }
        }
}
