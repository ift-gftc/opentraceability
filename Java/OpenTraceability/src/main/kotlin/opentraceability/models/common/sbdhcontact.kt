package models.common

import Constants
import utility.attributes.OpenTraceabilityAttribute

class SBDHContact {

    @OpenTraceabilityAttribute(Constants.SBDH_NAMESPACE, "Contact")
    var ContactName: String = ""

    @OpenTraceabilityAttribute(Constants.SBDH_NAMESPACE, "EmailAddress")
    var EmailAddress: String = ""
}
