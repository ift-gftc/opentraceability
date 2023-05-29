package opentraceability.utility

import com.fasterxml.jackson.core.JsonToken
import com.intellij.json.psi.JsonObject
import java.util.*

class JsonContextHelper {
    companion object {

        var _lock: Object = Object()
        var _contextCache: MutableMap<String,JsonObject> = mutableMapOf()

        fun GetJsonLDContext(contextURL: String): JsonObject {
            TODO("Not yet implemented")
        }

        fun ScrapeNamespaces(jContext: JsonObject): MutableMap<String, String> {
            TODO("Not yet implemented")
        }

        fun IsNamespace(value: String): Boolean {
            TODO("Not yet implemented")
        }


        fun ExpandVocab(
            json: JsonToken,
            jcontext: JsonObject,
            namespaces: MutableMap<String, String>,
            jvocabcontext: JsonObject?
        ): JsonToken? {
            TODO("Not yet implemented")
        }


        fun CompressVocab(
            json: JsonToken,
            jcontext: JsonObject,
            namespaces: MutableMap<String, String>,
            jvocabcontext: JsonObject?
        ): JsonToken? {
            TODO("Not yet implemented")
        }


        fun ModifyVocab(
            json: JsonToken,
            jcontext: JsonObject,
            namespaces: MutableMap<String, String>,
            namespacesReverse: MutableMap<String, String>,
            transformType: JsonLDVocabTransformationType,
            jvocabcontext: JsonObject? = null
        ): JsonToken? {
            TODO("Not yet implemented")
        }

       internal  fun IsComplexContext(jobj: JsonObject): Boolean {
            TODO("Not yet implemented")
        }

    }
}
