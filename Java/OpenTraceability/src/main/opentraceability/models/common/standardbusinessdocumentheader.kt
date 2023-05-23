package models.common

class StandardBusinessDocumentHeader {

    //TODO: review this file

    companion object {
        var DummyHeader: StandardBusinessDocumentHeader = StandardBusinessDocumentHeader()
    }

    //[OpenTraceability(Constants.SBDH_NAMESPACE, "HeaderVersion")]
    var HeaderVersion: String = ""

    //[OpenTraceabilityObject]
    //[OpenTraceability(Constants.SBDH_NAMESPACE, "Sender")]
    var Sender: SBDHOrganization? = null

    //[OpenTraceabilityObject]
    //[OpenTraceability(Constants.SBDH_NAMESPACE, "Receiver")]
    var Receiver: SBDHOrganization? = null

    //[OpenTraceabilityObject]
    //[OpenTraceability(Constants.SBDH_NAMESPACE, "DocumentIdentification")]
    var DocumentIdentification: SBDHDocumentIdentification? = null

}
