package mappers.epcis.json

import interfaces.IVocabularyElement
import models.events.*
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import kotlin.reflect.KClass

class EPCISJsonMasterDataWriter {
    companion object {
    }

    fun WriteMasterData(jDoc: JSONObject, doc: EPCISBaseDocument) {
        TODO("Not yet implemented")
    }

    internal fun WriteMasterDataList(data: List<IVocabularyElement>, xVocabList: JSONArray, type: String) {
        TODO("Not yet implemented")
    }

    internal fun WriteMasterDataObject(md: IVocabularyElement): JSONObject {
        TODO("Not yet implemented")
        return JSONObject()
    }

    internal fun WriteObject(t: KClass<*>, o: Any): JSONObject {
        TODO("Not yet implemented")
        return JSONObject()
    }
}
