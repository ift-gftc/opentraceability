package opentraceability.mappers

import opentraceability.interfaces.IEPCISDocumentMapper
import opentraceability.mappers.epcis.json.EPCISDocumentJsonMapper
import opentraceability.mappers.epcis.xml.EPCISDocumentXMLMapper
import opentraceability.models.identifiers.*
import opentraceability.models.events.*

class EPCISDocumentMappers {
    var XML: IEPCISDocumentMapper = EPCISDocumentXMLMapper()
    var JSON: IEPCISDocumentMapper = EPCISDocumentJsonMapper()
}
