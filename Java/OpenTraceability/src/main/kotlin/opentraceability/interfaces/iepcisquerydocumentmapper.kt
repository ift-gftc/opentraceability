package interfaces

import models.events.*

interface IEPCISQueryDocumentMapper {
    fun Map(strValue: String, checkSchema: Boolean = true): EPCISQueryDocument
    fun Map(doc: EPCISQueryDocument): String
}
