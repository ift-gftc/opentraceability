package opentraceability.models.events
import opentraceability.utility.attributes.OpenTraceabilityAttribute
import opentraceability.utility.attributes.OpenTraceabilityJsonAttribute
import java.lang.reflect.Type
import java.net.URI
class EventSource {
    //TODO: review this

    @OpenTraceabilityJsonAttribute("type")
    @OpenTraceabilityAttribute("","@type")
    var Type: URI? = null


    @OpenTraceabilityJsonAttribute("source")
    @OpenTraceabilityAttribute("","text()")
    var Value: String? = null

    /*
    var ParsedType: EventSourceType = EventSourceType()
    public EventSourceType ParsedType
    {
        get
        {
            EventSourceType type = EventSourceType.Unknown;

            foreach (var e in Enum.GetValues<EventSourceType>())
            {
                if (EnumUtil.GetEnumAttributes<CBVAttribute>(e).Exists(e => e.Value.ToLower() == Type?.ToString().ToLower()))
                {
                    return e;
                }
            }

            return type;
        }
        set
        {
            string? t = EnumUtil.GetEnumAttributes<CBVAttribute>(value).Where(e => e.Value.StartsWith("urn")).FirstOrDefault()?.Value;
            if (!string.IsNullOrWhiteSpace(t))
            {
                this.Type = new Uri(t);
            }
        }
    }
    */
}
