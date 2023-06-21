package opentraceability.gdst.events;

import opentraceability.models.identifiers.PGLN;

public interface IGDSTEvent {
    public PGLN getProductOwner();
}
