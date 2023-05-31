package mappers

import interfaces.IEPCISDocumentMapper
import mappers.epcis.json.EPCISDocumentJsonMapper
import mappers.epcis.xml.EPCISDocumentXMLMapper

class EPCISDocumentMappers {
    var XML: IEPCISDocumentMapper = EPCISDocumentXMLMapper()
    var JSON: IEPCISDocumentMapper = EPCISDocumentJsonMapper()
}
