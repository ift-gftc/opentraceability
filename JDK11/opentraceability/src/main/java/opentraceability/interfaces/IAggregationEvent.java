package opentraceability.interfaces;

import opentraceability.models.identifiers.EPC;

public interface IAggregationEvent extends IEvent {
    EPC parentID = null;
}