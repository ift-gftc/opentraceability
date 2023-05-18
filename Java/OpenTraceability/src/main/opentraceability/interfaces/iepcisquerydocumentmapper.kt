package interfaces
import models.identifiers.*
import models.events.*
interface IEPCISQueryDocumentMapper {
    fun Map(strValue: String, checkSchema: Boolean): EPCISQueryDocument
    fun Map(doc: EPCISQueryDocument): String
}
