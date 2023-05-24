package models.events
import java.lang.reflect.Type
import java.net.URI
class EventSource {
    //TODO: review this

    //[OpenTraceabilityJson("type")]
    //[OpenTraceability("@type")]
    var Type: URI? = null


    //[OpenTraceabilityJson("source")]
    //[OpenTraceability("text()")]
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
