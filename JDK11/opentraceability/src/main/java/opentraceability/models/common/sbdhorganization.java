package opentraceability.models.common;

import opentraceability.Constants;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;
import opentraceability.utility.attributes.OpenTraceabilityObjectAttribute;

public class SBDHOrganization {
    @OpenTraceabilityAttribute(Constants.SBDH_NAMESPACE, "Identifier")
    public String Identifier = null;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(Constants.SBDH_NAMESPACE, "ContactInformation")
    public SBDHContact ContactInformation = new SBDHContact();
}