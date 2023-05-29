package models.common

import Constants
import utility.attributes.OpenTraceabilityAttribute
import utility.attributes.OpenTraceabilityObjectAttribute

class SBDHOrganization {

    @OpenTraceabilityAttribute(Constants.SBDH_NAMESPACE, "Identifier")
    var Identifier: String = ""


    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(Constants.SBDH_NAMESPACE, "ContactInformation")
    var ContactInformation: SBDHContact = SBDHContact()

}
