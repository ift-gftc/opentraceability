package mappers.epcis.json

import interfaces.IEPCISQueryDocumentMapper
import models.events.EPCISQueryDocument

class EPCISQueryDocumentJsonMapper : IEPCISQueryDocumentMapper {

    override fun Map(strValue: String, checkSchema: Boolean): EPCISQueryDocument {
        TODO("Not yet implemented")
    }

    override fun Map(doc: EPCISQueryDocument): String {
        TODO("Not yet implemented")
    }
}
