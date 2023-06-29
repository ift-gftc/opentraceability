package opentraceability.gdst.events.kdes;

import opentraceability.Constants;
import opentraceability.utility.attributes.*;

import java.util.ArrayList;
import java.util.List;

public class VesselCatchInformationList {
    @OpenTraceabilityArrayAttribute(itemType = VesselCatchInformation.class)
    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityJsonAttribute(name = "cbvmda:vesselCatchInformation")
    @OpenTraceabilityAttribute(ns = Constants.CBVMDA_NAMESPACE, name = "vesselCatchInformation")
    private List<VesselCatchInformation> vessels = new ArrayList<>();

    // Add constructor, getters, and setters here
}
