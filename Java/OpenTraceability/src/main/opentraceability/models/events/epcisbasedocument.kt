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
    var Events: ArrayList<IEvent> = ArrayList<IEvent>()
    var MasterData: ArrayList<IVocabularyElement> = ArrayList<IVocabularyElement>()
    var Namespaces: MutableMap<String, String> = mutableMapOf<String, String>()
    var Contexts: ArrayList<String> = ArrayList<String>()
    var Attributes: MutableMap<String, String> = mutableMapOf<String, String>()

    fun<T> GetMasterData(id: String?): T{
        TODO("Not yet implemented")
    }

    fun Merge(data: EPCISBaseDocument) {
        TODO("Not yet implemented")
    }

    fun FilterEvents(parameters: EPCISQueryParameters): ArrayList<IEvent> {
        TODO("Not yet implemented")
    }

    fun HasMatch(evt: IEvent, epcs: ArrayList<String>, allowedTypes: ArrayList<EventProductType>): Boolean {
        TODO("Not yet implemented")
    }

    fun HasUriMatch(uri: URL?, filter: ArrayList<String>, prefix: String, replacePrefix: String): Boolean {
        TODO("Not yet implemented")
    }
}
