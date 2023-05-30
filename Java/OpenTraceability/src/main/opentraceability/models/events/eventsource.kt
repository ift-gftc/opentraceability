package models.events
import utility.CBVAttribute
import utility.EnumUtil
import utility.attributes.OpenTraceabilityAttribute
import utility.attributes.OpenTraceabilityJsonAttribute
import java.lang.reflect.Type
import java.net.URI
class EventSource {

    @OpenTraceabilityJsonAttribute("type")
    @OpenTraceabilityAttribute("","@type")
    var Type: URI? = null


    @OpenTraceabilityJsonAttribute("source")
    @OpenTraceabilityAttribute("","text()")
    var Value: String? = null

    val parsedType: EventSourceType
        get() {
            var type = EventSourceType.Unknown

            for (e in enumValues<EventSourceType>()) {
                if (EnumUtil.getEnumAttributes<CBVAttribute>(e).any { it.value.toLowerCase() == type?.toString()?.toLowerCase() }) {
                    return e
                }
            }

            return type
        }
        set(value) {
            val t = EnumUtil.getEnumAttributes<CBVAttribute>(value).firstOrNull { it.value.startsWith("urn") }?.value
            if (!t.isNullOrBlank()) {
                this.Type = URI(t)
            }
        }

}
