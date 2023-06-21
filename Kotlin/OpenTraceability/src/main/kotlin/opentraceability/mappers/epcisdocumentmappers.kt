package opentraceability.mappers

import opentraceability.interfaces.IEPCISDocumentMapper
import opentraceability.mappers.epcis.json.EPCISDocumentJsonMapper
import opentraceability.mappers.epcis.xml.EPCISDocumentXMLMapper

class EPCISDocumentMappers {
    var XML: IEPCISDocumentMapper = EPCISDocumentXMLMapper()
    var JSON: IEPCISDocumentMapper = EPCISDocumentJsonMapper()
}
