package interfaces

import models.events.*

interface IEPCISDocumentMapper {
    fun map(strValue: String): EPCISDocument
    fun map(doc: EPCISDocument): String
}
