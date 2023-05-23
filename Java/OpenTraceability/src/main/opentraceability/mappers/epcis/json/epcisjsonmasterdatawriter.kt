package mappers.epcis.json

import com.intellij.json.psi.JsonObject
import interfaces.IVocabularyElement
import models.events.*
import org.json.simple.JSONArray
import kotlin.reflect.KClass

class EPCISJsonMasterDataWriter {
    companion object {
        fun WriteMasterData(jDoc: JsonObject, doc: EPCISBaseDocument) {
            TODO("Not yet implemented")
        }

        internal fun WriteMasterDataList(data: List<IVocabularyElement>, xVocabList: JSONArray, type: String) {
            TODO("Not yet implemented")
        }

        internal fun WriteMasterDataObject(md: IVocabularyElement): JsonObject {
            TODO("Not yet implemented")
        }

        internal fun WriteObject(t: KClass<*>, o: Any): JsonObject {
            TODO("Not yet implemented")
        }
    }
}
