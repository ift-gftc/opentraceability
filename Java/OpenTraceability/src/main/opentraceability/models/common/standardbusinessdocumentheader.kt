package models.common
class StandardBusinessDocumentHeader {
    var HeaderVersion: String = ""
    var Sender: SBDHOrganization = SBDHOrganization()
    var Receiver: SBDHOrganization = SBDHOrganization()
    var DocumentIdentification: SBDHDocumentIdentification = SBDHDocumentIdentification()
    var DummyHeader: StandardBusinessDocumentHeader = StandardBusinessDocumentHeader()
    companion object{
        var DummyHeader: StandardBusinessDocumentHeader = StandardBusinessDocumentHeader()
    }
}
