package mappers

import interfaces.IEPCISQueryDocumentMapper
import mappers.epcis.json.EPCISQueryDocumentJsonMapper
import mappers.epcis.xml.EPCISQueryDocumentXMLMapper

class EPCISQueryDocumentMappers {
    var XML: IEPCISQueryDocumentMapper = EPCISQueryDocumentXMLMapper()
    var JSON: IEPCISQueryDocumentMapper = EPCISQueryDocumentJsonMapper()
}
