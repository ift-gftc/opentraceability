package opentraceability.models.events;

import java.net.URI;
import opentraceability.utility.attributes.OpenTraceabilityArrayAttribute;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;

public class PersistentDisposition {
    @OpenTraceabilityAttribute("", "unset", 1)
    @OpenTraceabilityArrayAttribute
    public MutableList<URI> unset = null;

    @OpenTraceabilityAttribute("", "set", 2)
    @OpenTraceabilityArrayAttribute
    public MutableList<URI> set = null;
}