package mappers.epcis.json

import interfaces.IVocabularyElement
import models.events.*
import org.json.simple.JSONObject
import kotlin.reflect.KClass
import kotlin.reflect.KType

class EPCISJsonMasterDataReader {
    companion object {
    }

    fun ReadMasterData(doc: EPCISBaseDocument, jMasterData: JSONObject) {
        TODO("Not yet implemented")

    }

    internal fun ReadTradeitem(doc: EPCISBaseDocument, xTradeitem: JSONObject, type: String) {

        TODO("Not yet implemented")
    }

    internal fun ReadLocation(doc: EPCISBaseDocument, xLocation: JSONObject, type: String) {

        TODO("Not yet implemented")
    }

    internal fun ReadTradingParty(doc: EPCISBaseDocument, xTradingParty: JSONObject, type: String) {

        TODO("Not yet implemented")
    }

    internal fun ReadUnknown(doc: EPCISBaseDocument, xVocabElement: JSONObject, type: String) {

        TODO("Not yet implemented")
    }

    internal fun ReadMasterDataObject(md: IVocabularyElement, jMasterData: JSONObject, readKDEs: Boolean = true) {

        TODO("Not yet implemented")
    }

    internal fun ReadKDEObject(doc: EPCISBaseDocument, xTradeitem: JSONObject, type: String): Any {
        TODO("Not yet implemented")
        return ""
    }

    internal fun TrySetValueType(value: String, p: KType, o: Any): Boolean {
        TODO("Not yet implemented")
        return false
    }
}