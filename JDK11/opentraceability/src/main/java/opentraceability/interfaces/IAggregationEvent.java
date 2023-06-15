package opentraceability.interfaces;

import opentraceability.models.identifiers.EPC;

public abstract class IAggregationEvent<T> extends IILMDEvent<T> {
    EPC parentID = null;
}