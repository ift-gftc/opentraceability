package opentraceability.models.common;

import opentraceability.Constants;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;
import opentraceability.utility.attributes.OpenTraceabilityObjectAttribute;

public class SBDHOrganization {
    @OpenTraceabilityAttribute(ns = Constants.SBDH_NAMESPACE, name = "Identifier")
    public String Identifier = null;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(ns = Constants.SBDH_NAMESPACE, name = "ContactInformation")
    public SBDHContact ContactInformation = new SBDHContact();
}