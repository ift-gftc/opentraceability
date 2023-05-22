package models.events
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
    var Namespaces: System.Collections.Generic.Dictionary<String,String> = System.Collections.Generic.Dictionary<String,String>()
    var Contexts: List<String> = ArrayList<String>()
    var Attributes: System.Collections.Generic.Dictionary<String,String> = System.Collections.Generic.Dictionary<String,String>()
    companion object{
    }
}
