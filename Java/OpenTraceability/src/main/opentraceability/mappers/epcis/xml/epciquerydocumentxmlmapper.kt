package opentraceability.mappers.epcis.xml

import opentraceability.interfaces.IEPCISQueryDocumentMapper
import opentraceability.models.events.*
import opentraceability.models.events.EPCISQueryDocument

class EPCISQueryDocumentXMLMapper : IEPCISQueryDocumentMapper {

    override fun Map(strValue: String, checkSchema: Boolean): EPCISQueryDocument {
        TODO("Not yet implemented")
    }

    override fun Map(doc: EPCISQueryDocument): String {
        TODO("Not yet implemented")
    }
}