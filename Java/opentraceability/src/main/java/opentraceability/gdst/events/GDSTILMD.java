package opentraceability.gdst.events;

import opentraceability.Constants;
import opentraceability.gdst.events.kdes.VesselCatchInformationList;
import opentraceability.models.events.EventILMD;
import opentraceability.utility.attributes.*;

public class GDSTILMD extends EventILMD {
    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(ns = Constants.CBVMDA_NAMESPACE, name = "vesselCatchInformationList")
    @OpenTraceabilityJsonAttribute(name = "cbvmda:vesselCatchInformationList")
    private VesselCatchInformationList vesselCatchInformationList;

    @OpenTraceabilityAttribute(ns = Constants.GDST_NAMESPACE, name = "broodstockSource")
    @OpenTraceabilityJsonAttribute(name = "gdst:broodstockSource")
    private String broodstockSource;

    @OpenTraceabilityAttribute(ns = Constants.GDST_NAMESPACE, name = "aquacultureMethod")
    @OpenTraceabilityJsonAttribute(name = "gdst:aquacultureMethod")
    private String aquacultureMethod;

    @OpenTraceabilityAttribute(ns = Constants.GDST_NAMESPACE, name = "proteinSource")
    @OpenTraceabilityJsonAttribute(name = "gdst:proteinSource")
    private String proteinSource;

    // Add constructor, getters, and setters here
}





