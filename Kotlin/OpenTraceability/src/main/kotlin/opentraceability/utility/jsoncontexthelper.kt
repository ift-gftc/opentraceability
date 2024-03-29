package opentraceability.utility

import okhttp3.*
import java.util.*
import org.json.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.regex.Pattern
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request

class JsonContextHelper {

    companion object {


        private val lock = Any()
        private val contextCache: ConcurrentMap<String, JSONObject> = ConcurrentHashMap()

        fun getJsonLDContext(contextURL: String): JSONObject {
            var jContext: JSONObject? = contextCache[contextURL]
            if (jContext == null) {



                val httpUrl = contextURL.toHttpUrlOrNull()
                    ?: throw IllegalArgumentException("Invalid URL: $contextURL")


                synchronized(lock) {
                    val client = OkHttpClient()
                    val request = Request.Builder()
                        .url(contextURL)
                        .build()
                    val response: Response = client.newCall(request).execute()
                    val jsonString: String =
                        response.body?.string() ?: throw Exception("Failed to fetch JSON-LD context from $contextURL.")
                    jContext = JSONObject(jsonString)
                    contextCache[contextURL] = jContext
                }
            }

            return jContext!!.getJSONObject("@context")
                ?: throw Exception("Failed to fetch JSON-LD context from $contextURL.")
        }

        fun scrapeNamespaces(jContext: JSONObject): MutableMap<String, String> {
            val namespaces: MutableMap<String, String> = HashMap()
            val keys: Iterator<String> = jContext.keys()
            while (keys.hasNext()) {
                val key: String = keys.next()
                val value: Any? = jContext.get(key)
                if (value is JSONObject) {
                    continue
                }
                val strValue: String? = value?.toString()
                if (strValue != null && isNamespace(strValue)) {
                    namespaces[key] = strValue.trimEnd(':')
                }
            }
            return namespaces
        }

        fun isNamespace(value: String): Boolean {
            val reg: Pattern = Pattern.compile("^urn:[a-z0-9][a-z0-9-]{0,31}:[a-z0-9()+,\\-.:=@;\$_!*'%\\/?#]+\$")
            return if (value.matches(Regex("^https?://.*$"))) {
                true
            } else reg.matcher(value).matches()
        }

        fun expandVocab(
            json: Any,
            jContext: JSONObject,
            namespaces: MutableMap<String, String>,
            jVocabContext: JSONObject? = null
        ): Any? {
            return modifyVocab(
                json,
                jContext,
                namespaces,
                namespaces.reverse(),
                JsonLDVocabTransformationType.Expand,
                jVocabContext
            )
        }

        fun compressVocab(
            json: Any,
            jContext: JSONObject,
            namespaces: MutableMap<String, String>,
            jVocabContext: JSONObject? = null
        ): Any? {
            return modifyVocab(
                json,
                jContext,
                namespaces,
                namespaces.reverse(),
                JsonLDVocabTransformationType.Compress,
                jVocabContext
            )
        }

        fun modifyVocab(
            json: Any,
            jContext: JSONObject,
            namespaces: MutableMap<String, String>,
            namespacesReverse: MutableMap<String, String>,
            transformType: JsonLDVocabTransformationType,
            jVocabContext: JSONObject? = null
        ): Any? {
            return when (json) {
                is JSONObject -> {
                    val jObj: JSONObject = json
                    for (key: String in jObj.keys()) {
                        val jContextProp: JSONObject? = jContext.optJSONObject(key)
                        if (jContextProp != null) {
                            val jPropValue: Any? = jObj.opt(key)
                            val jPropContext: Any? = jContextProp.opt("@context") ?: jVocabContext
                            if (jPropValue != null && jPropContext != null) {
                                if (jContextProp.optString("@type") == "@vocab") {
                                    if (jPropContext is JSONObject) {
                                        if (jPropValue is JSONArray) {
                                            val jArr: JSONArray = jPropValue
                                            for (i in 0 until jArr.length()) {
                                                val jt: Any = jArr[i]
                                                val newValue: Any? = modifyVocab(
                                                    jt,
                                                    jPropContext,
                                                    namespaces,
                                                    namespacesReverse,
                                                    transformType,
                                                    jVocabContext
                                                )
                                                if (newValue != null) {
                                                    jArr.put(i, newValue)
                                                }
                                            }
                                        } else {
                                            val newValue: Any? = modifyVocab(
                                                jPropValue,
                                                jPropContext,
                                                namespaces,
                                                namespacesReverse,
                                                transformType,
                                                jVocabContext
                                            )
                                            if (newValue != null) {
                                                jObj.put(key, newValue)
                                            }
                                        }
                                    } else throw Exception("jContextProp has @type set to @vocab, but the @context is not a JSONObject. jContextProp=$jContextProp and jPropValue=$jPropValue")
                                } else if (jPropValue is JSONArray) {
                                    val jArr: JSONArray = jPropValue
                                    val jPropContextObj: JSONObject = jPropContext as JSONObject
                                    for (i in 0 until jArr.length()) {
                                        val jt: Any = jArr[i]
                                        if (jt is JSONObject) {
                                            val jItem: JSONObject = jt
                                            modifyVocab(
                                                jItem,
                                                jPropContextObj,
                                                namespaces,
                                                namespacesReverse,
                                                transformType,
                                                jVocabContext
                                            )
                                        } else {
                                            val newValue: Any? = modifyVocab(
                                                jt,
                                                jPropContextObj,
                                                namespaces,
                                                namespacesReverse,
                                                transformType,
                                                jVocabContext
                                            )
                                            if (newValue != null) {
                                                jArr.put(i, newValue)
                                            }
                                        }
                                    }
                                } else if (jPropValue is JSONObject) {
                                    val jPropContextObj: JSONObject =
                                        if (jPropContext is JSONArray) jPropContext[1] as JSONObject else jPropContext as JSONObject
                                    modifyVocab(
                                        jPropValue,
                                        jPropContextObj,
                                        namespaces,
                                        namespacesReverse,
                                        transformType,
                                        jVocabContext
                                    )
                                }
                            }
                        }
                    }
                    jObj
                }

                else -> {
                    val jc: JSONObject = jVocabContext ?: jContext
                    val value: String? = json.toString()
                    return if (value == null) {
                        null
                    } else if (transformType == JsonLDVocabTransformationType.Expand) {
                        val jMapping: Any? = jc.opt(value)
                        if (jMapping != null) {
                            val uri: String = jMapping.toString()
                            val parts: MutableList<String> = uri.split(":").toMutableList()
                            val ns: String = namespaces[parts[0]]!!
                            val newValue: String = ns + parts[1]
                            newValue
                        } else {
                            null
                        }
                    } else if (transformType == JsonLDVocabTransformationType.Compress) {
                        val ns: String = value.substring(0, value.lastIndexOf('/') + 1)
                        if (namespacesReverse.containsKey(ns)) {
                            val compressedValue: String = value.replace(ns, namespacesReverse[ns] + ":")
                            val compressedVocab: String? =
                                jc.keys().asSequence().firstOrNull { jc.getString(it) == compressedValue }
                            compressedVocab
                        } else {
                            value
                        }
                    } else {
                        throw Exception("Unrecognized JSON-LD Vocab Transformation Type = $transformType")
                    }
                }
            }
        }

        internal fun isComplexContext(jObj: JSONObject): Boolean {
            val keys: Iterator<String> = jObj.keys()
            while (keys.hasNext()) {
                val key: String = keys.next()
                val value: Any? = jObj.opt(key)
                if (value is JSONObject) {
                    return true
                }
                val strValue: String? = value?.toString()
                if (strValue == null || !isNamespace(strValue)) {
                    return true
                }
            }
            return false
        }
    }

}
