package models.common

import Constants
import utility.attributes.*
import java.time.OffsetDateTime

class StandardBusinessDocumentHeader {

    @OpenTraceabilityAttribute(Constants.SBDH_NAMESPACE, "HeaderVersion")
    var HeaderVersion: String = ""

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(Constants.SBDH_NAMESPACE, "Sender")
    var Sender: SBDHOrganization? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(Constants.SBDH_NAMESPACE, "Receiver")
    var Receiver: SBDHOrganization? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(Constants.SBDH_NAMESPACE, "DocumentIdentification")
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
