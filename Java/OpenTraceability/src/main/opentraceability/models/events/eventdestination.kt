package models.events

import java.lang.reflect.Type
import java.net.URI

class EventDestination {

    //TODO: review this

    //[OpenTraceabilityJson("type")]
    //[OpenTraceability("@type")]
    var Type: URI? = null

    //[OpenTraceabilityJson("destination")]
    //[OpenTraceability("text()")]
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
