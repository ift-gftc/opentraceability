package utility
import com.fasterxml.jackson.core.JsonToken
import com.intellij.json.psi.JsonObject
import java.util.*
class JsonContextHelper {
    companion object{
    }


    fun GetJsonLDContext(contextURL: String): JsonObject {
        TODO("Not yet implemented")
    }


    fun<String> ScrapeNamespaces(jContext: JsonObject): MutableMap<String, String> {
        TODO("Not yet implemented")
    }

    fun IsNamespace(value: String): Boolean {
        TODO("Not yet implemented")
    }


    fun ExpandVocab(json: JsonToken, jcontext: JsonObject, namespaces: MutableMap<String, String>, jvocabcontext: JsonObject): JsonToken {
        TODO("Not yet implemented")
    }


    fun CompressVocab(json: JsonToken, jcontext: JsonObject, namespaces: MutableMap<String, String>, jvocabcontext: JsonObject): JsonToken {
        TODO("Not yet implemented")
    }
}
