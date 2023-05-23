package models.common

class SBDHOrganization {

    //[OpenTraceability(Constants.SBDH_NAMESPACE, "Identifier")]
    var Identifier: String = ""


    //[OpenTraceabilityObject]
    //[OpenTraceability(Constants.SBDH_NAMESPACE, "ContactInformation")]
    var ContactInformation: SBDHContact = SBDHContact()

}
