package opentraceability.models.events;

import java.net.URI;
import java.util.ArrayList;

import opentraceability.utility.attributes.OpenTraceabilityArrayAttribute;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;

public class PersistentDisposition {
    @OpenTraceabilityAttribute(ns="", name="unset", sequenceOrder = 1)
    @OpenTraceabilityArrayAttribute(itemType = URI.class)
    public ArrayList<URI> unset = null;

    @OpenTraceabilityAttribute(ns="", name="set", sequenceOrder = 2)
    @OpenTraceabilityArrayAttribute(itemType = URI.class)
    public ArrayList<URI> set = null;
}