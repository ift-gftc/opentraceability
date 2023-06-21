package opentraceability.models.common

import opentraceability.Constants
import opentraceability.utility.attributes.OpenTraceabilityAttribute

class SBDHContact {

    @OpenTraceabilityAttribute(opentraceability.Constants.SBDH_NAMESPACE, "Contact")
    var ContactName: String = ""

    @OpenTraceabilityAttribute(opentraceability.Constants.SBDH_NAMESPACE, "EmailAddress")
    var EmailAddress: String = ""
}
