package models.events

import interfaces.IEvent
import interfaces.IVocabularyElement
import models.common.StandardBusinessDocumentHeader
import java.util.*
import models.identifiers.*
import models.events.*
import queries.EPCISQueryParameters
import java.net.URL
import java.time.OffsetDateTime

open class EPCISBaseDocument {
    var EPCISVersion: EPCISVersion? = null
    var CreationDate: OffsetDateTime? = null
    var Header: StandardBusinessDocumentHeader? = null
    var Events: List<IEvent> = ArrayList<IEvent>()
    var MasterData: List<IVocabularyElement> = ArrayList<IVocabularyElement>()
    var Namespaces: MutableMap<String, String> = mutableMapOf<String, String>()
    var Contexts: List<String> = ArrayList<String>()
    var Attributes: MutableMap<String, String> = mutableMapOf<String, String>()

    fun<T> GetMasterData(id: String?): T{
        TODO("Not yet implemented")
    }

    fun Merge(data: EPCISBaseDocument) {
        TODO("Not yet implemented")
    }

    fun FilterEvents(parameters: EPCISQueryParameters): List<IEvent> {
        TODO("Not yet implemented")
    }

    fun HasMatch(evt: IEvent, epcs: List<String>, allowedTypes: List<EventProductType>): Boolean {
        TODO("Not yet implemented")
    }

    fun HasUriMatch(uri: URL?, filter: List<String>, prefix: String, replacePrefix: String): Boolean {
        TODO("Not yet implemented")
    }
}
