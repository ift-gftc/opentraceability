package opentraceability.models.events;

import opentraceability.utility.attributes.OpenTraceabilityAttribute;
import opentraceability.utility.attributes.OpenTraceabilityJsonAttribute;

import java.net.URI;

public class EventBusinessTransaction {

    @OpenTraceabilityJsonAttribute("type")
    @OpenTraceabilityAttribute("", "@type")
    public URI type;

    @OpenTraceabilityJsonAttribute("bizTransaction")
    @OpenTraceabilityAttribute("", "text()")
    public String value;

}