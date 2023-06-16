package opentraceability.models.common;

import opentraceability.Constants;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;

public class SBDHContact {

    @OpenTraceabilityAttribute(ns=Constants.SBDH_NAMESPACE, name="Contact")
    public String ContactName = "";

    @OpenTraceabilityAttribute(ns=Constants.SBDH_NAMESPACE, name="EmailAddress")
    public String EmailAddress = "";
}