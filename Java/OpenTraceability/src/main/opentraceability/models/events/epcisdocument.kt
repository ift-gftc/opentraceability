package models.events
import interfaces.IEvent
import interfaces.IVocabularyElement
import models.common.StandardBusinessDocumentHeader
import java.util.*
import models.identifiers.*
import models.events.*
import java.time.OffsetDateTime
class EPCISDocument {
    var EPCISVersion: EPCISVersion? = null
    var CreationDate: OffsetDateTime? = null
    var Header: StandardBusinessDocumentHeader = StandardBusinessDocumentHeader()
    var Events: List<IEvent> = ArrayList<IEvent>()
    var MasterData: List<IVocabularyElement> = ArrayList<IVocabularyElement>()
    var Namespaces: Map<String, String> = mapOf<String, String>()
    var Contexts: List<String> = ArrayList<String>()
    var Attributes: Map<String, String> = mapOf<String, String>()
    companion object{
    }
}
