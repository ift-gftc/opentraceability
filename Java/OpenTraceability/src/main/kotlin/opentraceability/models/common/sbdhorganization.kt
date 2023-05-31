package models.common

import Constants
import utility.attributes.*

class SBDHOrganization {

    @OpenTraceabilityAttribute(Constants.SBDH_NAMESPACE, "Identifier")
    var Identifier: String = ""


    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(Constants.SBDH_NAMESPACE, "ContactInformation")
    var ContactInformation: SBDHContact = SBDHContact()

}
