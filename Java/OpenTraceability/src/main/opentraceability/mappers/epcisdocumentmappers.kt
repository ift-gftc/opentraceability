package mappers

import interfaces.IEPCISDocumentMapper
import mappers.epcis.json.EPCISDocumentJsonMapper
import mappers.epcis.xml.EPCISDocumentXMLMapper
import models.identifiers.*
import models.events.*

class EPCISDocumentMappers {
    var XML: IEPCISDocumentMapper = EPCISDocumentXMLMapper()
    var JSON: IEPCISDocumentMapper = EPCISDocumentJsonMapper()
}
