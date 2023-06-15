package opentraceability.models.common;

import opentraceability.Constants;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;

public class SBDHContact {

    @OpenTraceabilityAttribute(Constants.SBDH_NAMESPACE, "Contact")
    public String ContactName = "";

    @OpenTraceabilityAttribute(Constants.SBDH_NAMESPACE, "EmailAddress")
    public String EmailAddress = "";
}