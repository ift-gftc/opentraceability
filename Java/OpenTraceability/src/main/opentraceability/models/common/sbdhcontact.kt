package opentraceability.models.common

import opentraceability.Constants
import opentraceability.utility.attributes.OpenTraceabilityAttribute

class SBDHContact {

    @OpenTraceabilityAttribute(Constants.SBDH_NAMESPACE, "Contact")
    var ContactName: String = ""

    @OpenTraceabilityAttribute(Constants.SBDH_NAMESPACE, "EmailAddress")
    var EmailAddress: String = ""
}
