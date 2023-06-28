package opentraceability.models.events;

import opentraceability.models.identifiers.GLN;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;

public class EventLocation {

    @OpenTraceabilityAttribute(ns = "", name = "id")
    public GLN gln;

    public EventLocation() {}

    public EventLocation(GLN gln) {
        this.gln = gln;
    }
}