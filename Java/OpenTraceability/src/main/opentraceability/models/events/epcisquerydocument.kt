package models.events
import interfaces.IEvent
import interfaces.IVocabularyElement
import models.common.StandardBusinessDocumentHeader
import java.util.*
import models.identifiers.*
import models.events.*
import java.time.OffsetDateTime
class EPCISQueryDocument : EPCISBaseDocument() {
    var QueryName: String = ""
    var SubscriptionID: String = ""
    fun ToEPCISDocument(): EPCISQueryDocument{
        TODO("Not yet implemented")
    }

}
