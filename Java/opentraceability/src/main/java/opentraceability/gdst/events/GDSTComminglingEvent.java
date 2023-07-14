package opentraceability.gdst.events;

import opentraceability.Constants;
import opentraceability.models.events.TransformationEventBase;
import opentraceability.models.identifiers.PGLN;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;
import opentraceability.utility.attributes.OpenTraceabilityJsonAttribute;

import java.net.URI;

public class GDSTComminglingEvent extends TransformationEventBase<GDSTILMD> implements IGDSTEvent {
    @OpenTraceabilityAttribute(ns = Constants.GDST_NAMESPACE, name = "productOwner")
    @OpenTraceabilityJsonAttribute(name = "gdst:productOwner")
    private PGLN productOwner;
    @Override
    public PGLN getProductOwner() {
        return this.productOwner;
    }

    // Add constructor, getters, and setters here
    public GDSTComminglingEvent()
    {
        this.businessStep = URI.create("urn:gdst:bizStep:commingling");
    }
}
