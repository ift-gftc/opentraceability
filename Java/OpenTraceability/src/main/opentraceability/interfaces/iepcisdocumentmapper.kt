package interfaces
import models.identifiers.*
import models.events.*
interface IEPCISDocumentMapper {
    fun Map(strValue: String): EPCISDocument
    fun Map(doc: EPCISDocument): String
}
