package opentraceability.interfaces;

import opentraceability.models.events.EPCISVersion;
import opentraceability.models.events.EventILMD;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;
import opentraceability.utility.attributes.OpenTraceabilityObjectAttribute;

public abstract class ITransformationEvent<T extends EventILMD> extends IILMDEvent<T> {
    public String transformationID;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(ns = "", name = "ilmd", version = EPCISVersion.Any)
    public T ilmd = null;
}