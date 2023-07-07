package opentraceability.gdst.events;

import opentraceability.Constants;
import opentraceability.models.events.AggregationEventBase;
import opentraceability.models.events.EventAction;
import opentraceability.models.events.TransformationEvent;
import opentraceability.models.events.TransformationEventBase;
import opentraceability.models.identifiers.PGLN;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;
import opentraceability.utility.attributes.OpenTraceabilityJsonAttribute;

import java.net.URI;

public class GDSTFarmHarvestEvent extends TransformationEventBase<GDSTILMD> implements IGDSTEvent {
    @OpenTraceabilityAttribute(ns = Constants.GDST_NAMESPACE, name = "productOwner")
    @OpenTraceabilityJsonAttribute(name = "gdst:productOwner")
    private PGLN productOwner;

    @Override
    public PGLN getProductOwner() {
        return this.productOwner;
    }


    @OpenTraceabilityAttribute(ns = Constants.GDST_NAMESPACE, name = "humanWelfarePolicy")
    @OpenTraceabilityJsonAttribute(name = "gdst:humanWelfarePolicy")
    private String humanWelfarePolicy;

    // Add constructor, getters, and setters here
    public GDSTFarmHarvestEvent()
    {
        this.businessStep = URI.create("urn:gdst:bizStep:farmHarvest");
    }
}
