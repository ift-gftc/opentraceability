package mappers.epcis.json

import interfaces.IEPCISQueryDocumentMapper
import models.events.EPCISQueryDocument

class EPCISQueryDocumentJsonMapper : IEPCISQueryDocumentMapper {
    companion object {
    }

    override fun Map(strValue: String, checkSchema: Boolean): EPCISQueryDocument {
        TODO("Not yet implemented")
        return EPCISQueryDocument()
    }

    override fun Map(doc: EPCISQueryDocument): String {
        TODO("Not yet implemented")
        return ""
    }
}
