package opentraceability.models.events;

import java.net.URI;
import java.util.EnumSet;

import opentraceability.utility.CBVAttribute;
import opentraceability.utility.EnumUtil;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;
import opentraceability.utility.attributes.OpenTraceabilityJsonAttribute;

public class EventSource {
    @OpenTraceabilityJsonAttribute(name = "type")
    @OpenTraceabilityAttribute(ns = "", name = "@type")
    public URI type = null;

    @OpenTraceabilityJsonAttribute(name = "source")
    @OpenTraceabilityAttribute(ns = "", name = "text()")
    public String value = null;

    public EventSourceType getParsedType() throws NoSuchFieldException {
        EventSourceType type = EventSourceType.Unknown;
        for (EventSourceType e : EnumSet.allOf(EventSourceType.class)) {
            EventSourceType finalType = type;
            if (EnumUtil.GetEnumAttributes(e, CBVAttribute.class).stream().anyMatch(it -> it.value().equalsIgnoreCase(finalType.toString()))) {
                type = e;
                break;
            }
        }

        return type;
    }

    public void setParsedType(EventSourceType value) throws NoSuchFieldException {
        String t = EnumUtil.GetEnumAttributes(value, CBVAttribute.class).stream().filter(it -> it.value().startsWith("urn")).findFirst().get().value();

        if (t != null && !t.isEmpty()) {
            this.type = URI.create(t);
        }
    }
}