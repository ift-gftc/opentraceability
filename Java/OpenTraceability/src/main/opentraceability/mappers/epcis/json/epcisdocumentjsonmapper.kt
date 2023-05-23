package mappers.epcis.json

import interfaces.IEPCISDocumentMapper
import models.events.*

class EPCISDocumentJsonMapper : IEPCISDocumentMapper {
    companion object {
    }
    override fun Map(strValue: String): EPCISDocument {
        TODO("Not yet implemented")
        return EPCISDocument()
    }
    override fun Map(doc: EPCISDocument): String {
        TODO("Not yet implemented")
        return ""
    }
}
