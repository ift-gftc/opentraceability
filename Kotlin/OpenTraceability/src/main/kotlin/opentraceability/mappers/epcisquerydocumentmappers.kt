package opentraceability.mappers

import opentraceability.interfaces.IEPCISQueryDocumentMapper
import opentraceability.mappers.epcis.json.EPCISQueryDocumentJsonMapper
import opentraceability.mappers.epcis.xml.EPCISQueryDocumentXMLMapper

class EPCISQueryDocumentMappers {
    var XML: IEPCISQueryDocumentMapper = EPCISQueryDocumentXMLMapper()
    var JSON: IEPCISQueryDocumentMapper = EPCISQueryDocumentJsonMapper()
}
