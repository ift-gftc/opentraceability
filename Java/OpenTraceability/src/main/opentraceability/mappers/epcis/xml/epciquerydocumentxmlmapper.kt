package mappers.epcis.xml

import interfaces.IEPCISQueryDocumentMapper
import models.events.*
import models.events.EPCISQueryDocument

class EPCISQueryDocumentXMLMapper : IEPCISQueryDocumentMapper {

    override fun Map(strValue: String, checkSchema: Boolean): EPCISQueryDocument {
        TODO("Not yet implemented")
    }

    override fun Map(doc: EPCISQueryDocument): String {
        TODO("Not yet implemented")
    }
}