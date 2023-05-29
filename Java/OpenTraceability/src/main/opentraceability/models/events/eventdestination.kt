package opentraceability.models.events

import opentraceability.utility.attributes.OpenTraceabilityAttribute
import opentraceability.utility.attributes.OpenTraceabilityJsonAttribute
import java.lang.reflect.Type
import java.net.URI

class EventDestination {

    //TODO: review this

    @OpenTraceabilityJsonAttribute("type")
    @OpenTraceabilityAttribute("","@type")
    var Type: URI? = null

    @OpenTraceabilityJsonAttribute("destination")
    @OpenTraceabilityAttribute("","text()")
    var Value: String? = null


    /*
    var ParsedType: EventDestinationType = EventDestinationType()

    public EventDestinationType ParsedType
    {
        get
        {
            EventDestinationType type = EventDestinationType.Unknown;

            foreach (var e in Enum.GetValues<EventDestinationType>())
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
