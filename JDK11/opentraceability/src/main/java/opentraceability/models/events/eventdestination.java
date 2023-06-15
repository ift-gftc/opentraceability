package opentraceability.models.events;

import opentraceability.utility.CBVAttribute;
import opentraceability.utility.EnumUtil;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;
import opentraceability.utility.attributes.OpenTraceabilityJsonAttribute;

import java.net.URI;

public class EventDestination {
    @OpenTraceabilityJsonAttribute("type")
    @OpenTraceabilityAttribute("", "@type")
    public URI type;

    @OpenTraceabilityJsonAttribute("destination")
    @OpenTraceabilityAttribute("", "text()")
    public String value;

    public EventDestinationType getParsedType() {
        EventDestinationType type = EventDestinationType.Unknown;
        for (EventDestinationType e : EventDestinationType.values()) {
            for (CBVAttribute attribute : EnumUtil.GetEnumAttributes(e, CBVAttribute.class)) {
                if (attribute.value.toLowerCase().equals(type.toString().toLowerCase())) {
                    return e;
                }
            }
        }
        return type;
    }

    public void setParsedType(EventDestinationType value) {
        String t = EnumUtil.GetEnumAttributes(value, CBVAttribute.class).stream().filter(a -> a.value.startsWith("urn")).findFirst().get().value;
        if (!t.isBlank()) {
            this.type = URI.create(t);
        }
    }

}