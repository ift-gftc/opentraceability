package mappers.masterdata

import com.fasterxml.jackson.core.JsonToken
import com.intellij.util.containers.reverse
import interfaces.IMasterDataMapper
import interfaces.IVocabularyElement
import mappers.OpenTraceabilityJsonLDMapper
import org.codehaus.jettison.json.JSONObject
import utility.JsonContextHelper
import java.lang.reflect.Type
import java.util.*

class GS1VocabJsonMapper : IMasterDataMapper {
    override fun Map(vocab: IVocabularyElement): String {
        if (vocab.Context == null) {
            vocab.Context = JSONObject("""{
                                    "cbvmda": "urn:epcglobal:cbvmda:mda",
                                    "xsd": "http://www.w3.org/2001/XMLSchema#",
                                    "gs1": "http://gs1.org/voc/",
                                    "@vocab": "http://gs1.org/voc/",
                                    "gdst": "https://traceability-dialogue.org/vocab"
                                }""")
        }

        val namespaces = GetNamespaces(vocab.Context ?: throw Exception("vocab.Context is null."))
        val json = OpenTraceabilityJsonLDMapper.ToJson(vocab, namespaces.reverse()) as JSONObject? ?: throw Exception("Failed to map master data into GS1 web vocab.")
        json["@context"] = vocab.Context
        return json.toString()
    }

    override fun <T : IVocabularyElement> Map(value: String): T {
        return Map(T::class.java, value)
    }

    override fun Map(type: Class<*>, value: String): IVocabularyElement {
        val json = JSONObject(value)
        val namespaces = GetNamespaces(json["@context"] ?: throw Exception("@context is null on the JSON-LD when deserializing GS1 Web Vocab. $value"))
        val obj = OpenTraceabilityJsonLDMapper.FromJson(json, type, namespaces) as IVocabularyElement
        obj.Context = json["@context"]
        return obj
    }

    fun GetNamespaces(jContext: Any): MutableMap<String, String> {
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
