package models.common

class StandardBusinessDocumentHeader {

    companion object {
        var DummyHeader: StandardBusinessDocumentHeader = StandardBusinessDocumentHeader()
    }

    var HeaderVersion: String = ""
    var Sender: SBDHOrganization = SBDHOrganization()
    var Receiver: SBDHOrganization = SBDHOrganization()
    var DocumentIdentification: SBDHDocumentIdentification = SBDHDocumentIdentification()
    var DummyHeader: StandardBusinessDocumentHeader = StandardBusinessDocumentHeader()

}
