package opentraceability.models.events;

import opentraceability.utility.attributes.OpenTraceabilityAttribute;
import opentraceability.utility.attributes.OpenTraceabilityJsonAttribute;

import java.net.URI;

public class EventBusinessTransaction {

    @OpenTraceabilityJsonAttribute(name="type")
    @OpenTraceabilityAttribute(ns = "", name = "@type")
    public URI type;

    @OpenTraceabilityJsonAttribute(name="bizTransaction")
    @OpenTraceabilityAttribute(ns = "", name = "text()")
    public String value;
}