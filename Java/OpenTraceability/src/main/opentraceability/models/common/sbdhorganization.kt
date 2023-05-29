package opentraceability.models.common

import opentraceability.Constants
import opentraceability.utility.attributes.OpenTraceabilityAttribute
import opentraceability.utility.attributes.OpenTraceabilityObjectAttribute

class SBDHOrganization {

    @OpenTraceabilityAttribute(Constants.SBDH_NAMESPACE, "Identifier")
    var Identifier: String = ""


    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(Constants.SBDH_NAMESPACE, "ContactInformation")
    var ContactInformation: SBDHContact = SBDHContact()

}
