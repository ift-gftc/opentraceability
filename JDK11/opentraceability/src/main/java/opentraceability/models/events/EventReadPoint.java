package opentraceability.models.events;

import opentraceability.utility.attributes.OpenTraceabilityAttribute;

import java.net.URI;

public class EventReadPoint {

    @OpenTraceabilityAttribute(ns="", name="id")
    public URI ID = null;
}