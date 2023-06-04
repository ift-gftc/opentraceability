package opentraceability.models.common

import opentraceability.Constants
import opentraceability.utility.attributes.*

class SBDHOrganization {

    @OpenTraceabilityAttribute(opentraceability.Constants.SBDH_NAMESPACE, "Identifier")
    var Identifier: String = ""


    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(opentraceability.Constants.SBDH_NAMESPACE, "ContactInformation")
    var ContactInformation: SBDHContact = SBDHContact()

}
