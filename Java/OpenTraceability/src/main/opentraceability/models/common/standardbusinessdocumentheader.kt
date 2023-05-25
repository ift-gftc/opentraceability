package models.common

import java.time.OffsetDateTime

class StandardBusinessDocumentHeader {
    //TODO: review this file

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

    companion object {
        var DummyHeader: StandardBusinessDocumentHeader = StandardBusinessDocumentHeader()
            get() {
                var dh: StandardBusinessDocumentHeader = StandardBusinessDocumentHeader()
                dh.HeaderVersion = "x";

                dh.Sender = SBDHOrganization()
                dh.Sender!!.Identifier = "x"
                dh.Sender!!.ContactInformation = SBDHContact()
                dh.Sender!!.ContactInformation.ContactName = "x"
                dh.Sender!!.ContactInformation.EmailAddress = "x"

                dh.Receiver = SBDHOrganization()
                dh.Receiver!!.Identifier = "x"
                dh.Receiver!!.ContactInformation = SBDHContact()
                dh.Receiver!!.ContactInformation.ContactName = "x"
                dh.Receiver!!.ContactInformation.EmailAddress = "x"

                dh.DocumentIdentification = SBDHDocumentIdentification()
                dh.DocumentIdentification!!.CreationDateAndTime = OffsetDateTime.now()
                dh.DocumentIdentification!!.InstanceIdentifier = "x"
                dh.DocumentIdentification!!.MultipleType = "false"
                dh.DocumentIdentification!!.Standard = "x"
                dh.DocumentIdentification!!.Type = "x"
                dh.DocumentIdentification!!.TypeVersion = "3.0"

                return dh
            }
    }
}
