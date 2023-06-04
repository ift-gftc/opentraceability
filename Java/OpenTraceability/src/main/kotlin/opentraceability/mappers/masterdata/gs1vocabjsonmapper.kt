package opentraceability.mappers.masterdata

import opentraceability.interfaces.IMasterDataMapper
import opentraceability.interfaces.IVocabularyElement
import opentraceability.mappers.OpenTraceabilityJsonLDMapper
import org.json.JSONArray
import opentraceability.utility.JsonContextHelper
import java.lang.reflect.Type
import java.util.*
import org.json.JSONObject;
import kotlin.reflect.KClass

class GS1VocabJsonMapper : IMasterDataMapper {
    override fun map(vocab: IVocabularyElement): String {
        if (vocab.context == null) {
            vocab.context = JSONObject("""{
                                    "cbvmda": "urn:epcglobal:cbvmda:mda",
                                    "xsd": "http://www.w3.org/2001/XMLSchema#",
                                    "gs1": "http://gs1.org/voc/",
                                    "@vocab": "http://gs1.org/voc/",
                                    "gdst": "https://traceability-dialogue.org/vocab"
                                }""")
        }

        val namespaces = getNamespaces(vocab.context ?: throw Exception("vocab.Context is null."))
        val reversedNamespaces = namespaces.entries.associate { (key, value) -> value to key }.toMutableMap()


        val json = OpenTraceabilityJsonLDMapper.toJson(vocab, reversedNamespaces) as JSONObject? ?: throw Exception("Failed to map master data into GS1 web vocab.")
        json.put("@context", vocab.context)
        return json.toString()
    }

    override fun <T : IVocabularyElement> map(type: KClass<T>, value: String): IVocabularyElement {
        val json = JSONObject(value)
        val namespaces = getNamespaces(json["@context"] ?: throw Exception("@context is null on the JSON-LD when deserializing GS1 Web Vocab. $value"))
        val obj = OpenTraceabilityJsonLDMapper.fromJson(json, type, namespaces) as IVocabularyElement
        obj.context = json.get("@context") as JSONObject
        return obj
    }

    override fun <T : IVocabularyElement> map(value: String): IVocabularyElement {
        return map<T>(value)
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
