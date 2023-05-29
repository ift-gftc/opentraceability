package interfaces

import models.events.*
import models.events.EPCISQueryDocument

interface IEPCISQueryDocumentMapper {
    fun Map(strValue: String, checkSchema: Boolean = true): EPCISQueryDocument
    fun Map(doc: EPCISQueryDocument): String
}
