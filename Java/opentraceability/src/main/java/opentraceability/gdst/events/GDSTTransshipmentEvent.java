package opentraceability.gdst.events;

import opentraceability.Constants;
import opentraceability.models.events.EventAction;
import opentraceability.models.events.ObjectEventBase;
import opentraceability.models.identifiers.PGLN;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;
import opentraceability.utility.attributes.OpenTraceabilityJsonAttribute;

import java.net.URI;

public class GDSTTransshipmentEvent extends ObjectEventBase<GDSTILMD> implements IGDSTEvent {
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

    @OpenTraceabilityJsonAttribute(name = "cbvmda:unloadingPort")
    @OpenTraceabilityAttribute(ns = Constants.CBVMDA_NAMESPACE, name = "unloadingPort")
    private String unloadingPort;

    // Add constructor, getters, and setters here
    public GDSTTransshipmentEvent()
    {
        this.businessStep = URI.create("urn:gdst:bizStep:transshipment");
        this.action = EventAction.OBSERVE;
    }
}
