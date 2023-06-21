package opentraceability.interfaces;

import opentraceability.models.events.EPCISVersion;
import opentraceability.models.events.EventILMD;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;
import opentraceability.utility.attributes.OpenTraceabilityObjectAttribute;

public abstract class IILMDEvent<T extends EventILMD> extends IEvent {
    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(ns = "", name = "ilmd", version = EPCISVersion.V2)
    @OpenTraceabilityAttribute(ns = "", name = "extension/ilmd", version = EPCISVersion.V1)
    public T ilmd = null;
}