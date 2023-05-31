package mappers.masterdata

import interfaces.IMasterDataMapper
import interfaces.IVocabularyElement
import mappers.OpenTraceabilityJsonLDMapper
import org.json.JSONArray
import utility.JsonContextHelper
import java.lang.reflect.Type
import java.util.*
import org.json.JSONObject;
import kotlin.reflect.KClass

class GS1VocabJsonMapper : IMasterDataMapper {
    override fun map(vocab: IVocabularyElement): String {
        if (vocab.Context == null) {
            vocab.Context = JSONObject("""{
                                    "cbvmda": "urn:epcglobal:cbvmda:mda",
                                    "xsd": "http://www.w3.org/2001/XMLSchema#",
                                    "gs1": "http://gs1.org/voc/",
                                    "@vocab": "http://gs1.org/voc/",
                                    "gdst": "https://traceability-dialogue.org/vocab"
                                }""")
        }

        val namespaces = getNamespaces(vocab.Context ?: throw Exception("vocab.Context is null."))
        val reversedNamespaces = namespaces.entries.associate { (key, value) -> value to key }.toMutableMap()


        val json = OpenTraceabilityJsonLDMapper.toJson(vocab, reversedNamespaces) as JSONObject? ?: throw Exception("Failed to map master data into GS1 web vocab.")
        json.put("@context", vocab.Context)
        return json.toString()
    }

    override fun <T : IVocabularyElement> map(type: Class<T>, value: String): IVocabularyElement {
        val json = JSONObject(value)
        val namespaces = getNamespaces(json["@context"] ?: throw Exception("@context is null on the JSON-LD when deserializing GS1 Web Vocab. $value"))
        val obj = OpenTraceabilityJsonLDMapper.fromJson(json, type, namespaces) as IVocabularyElement
        obj.Context = json.get("@context") as JSONObject
        return obj
    }

    override inline fun <reified T : IVocabularyElement> map(value: String): IVocabularyElement {
        return map(T::class.java, value)
    }


    fun getNamespaces(jContext: Any): MutableMap<String, String> {
        val namespaces = mutableMapOf<String, String>()
        when (jContext) {
            is JSONObject -> {
                namespaces.putAll(JsonContextHelper.scrapeNamespaces(jContext))
            }
            is JSONArray -> {
                for (j in jContext) {
                    val ns = JsonContextHelper.scrapeNamespaces(j as JSONObject)
                    for ((k, v) in ns) {
                        if (!namespaces.containsKey(k)) {
                            namespaces[k] = v
                        }
                    }
                }
            }
        }
        return namespaces
    }
}
