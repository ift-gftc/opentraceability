package mappers.epcis.json

import com.fasterxml.jackson.core.JsonToken
import com.intellij.json.psi.JsonObject
import interfaces.IVocabularyElement
import models.events.*
import java.lang.reflect.Type

class EPCISJsonMasterDataReader {
    companion object {

        fun ReadMasterData(doc: EPCISBaseDocument, jMasterData: JsonObject) {
            TODO("Not yet implemented")

        }

        internal fun ReadTradeitem(doc: EPCISBaseDocument, xTradeitem: JsonObject, type: String) {
            TODO("Not yet implemented")
        }

        internal fun ReadLocation(doc: EPCISBaseDocument, xLocation: JsonObject, type: String) {
            TODO("Not yet implemented")
        }

        internal fun ReadTradingParty(doc: EPCISBaseDocument, xTradingParty: JsonObject, type: String) {
            TODO("Not yet implemented")
        }

        internal fun ReadUnknown(doc: EPCISBaseDocument, xVocabElement: JsonObject, type: String) {
            TODO("Not yet implemented")
        }

        internal fun ReadMasterDataObject(md: IVocabularyElement, jMasterData: JsonObject, readKDEs: Boolean = true) {
            TODO("Not yet implemented")
        }

        internal fun ReadKDEObject(j: JsonToken, t: Type): Object {
            TODO("Not yet implemented")
        }

        internal fun TrySetValueType(value: String, p: Type, o: Any): Boolean {
            TODO("Not yet implemented")
        }
    }
}