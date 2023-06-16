package opentraceability.interfaces;

import opentraceability.models.events.EventILMD;

public abstract class ITransformationEvent<T extends EventILMD> extends IILMDEvent<T> {
    public String transformationID;
}