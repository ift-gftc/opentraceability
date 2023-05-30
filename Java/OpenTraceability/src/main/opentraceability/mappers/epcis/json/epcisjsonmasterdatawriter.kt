package mappers.epcis.json

import com.intellij.json.psi.JsonObject
import interfaces.IVocabularyElement
import mappers.OTMappingTypeInformation
import models.common.LanguageString
import models.events.*
import models.events.EPCISBaseDocument
import org.json.simple.JSONArray
import utility.attributes.OpenTraceabilityArrayAttribute
import utility.attributes.OpenTraceabilityAttribute
import utility.attributes.OpenTraceabilityObjectAttribute
import kotlin.reflect.KClass

object EPCISJsonMasterDataWriter {
    fun WriteMasterData(jDoc: JObject, doc: EPCISBaseDocument) {
        if (doc.MasterData.isNotEmpty()) {
            val xEPCISHeader = jDoc["epcisHeader"] as JObject?
            if (xEPCISHeader == null) {
                jDoc["epcisHeader"] = JObject(
                    JProperty("epcisMasterData", JObject(JProperty("vocabularyList", JArray())))
                )
            } else {
                xEPCISHeader["epcisMasterData"] = JObject(JProperty("vocabularyList", JArray()))
            }
            val jVocabList = jDoc["epcisHeader"]?.get("epcisMasterData")?.get("vocabularyList") as JArray?
                ?: throw Exception("Failed to grab json object epcisHeader.epcisMasterData.vocabularyList")

            for ((type, mdList) in doc.MasterData.groupBy { it.epcisType }) {
                if (type != null) {
                    WriteMasterDataList(mdList.toMutableList(), jVocabList, type)
                } else {
                    throw Exception("There are master data vocabulary elements where the Type is NULL.")
                }
            }
        }
    }

    fun WriteMasterDataList(data: MutableList<IVocabularyElement>, xVocabList: JArray, type: String) {
        if (data.isNotEmpty()) {
            val jVocab = JObject(JProperty("type", type), JProperty("vocabularyElementList", JArray()))
            val xVocabEleList = jVocab["vocabularyElementList"] as JArray?
                ?: throw Exception("Failed to grab the array vocabularyElementList")

            for (md in data) {
                val xMD = WriteMasterDataObject(md)
                xVocabEleList.add(xMD)
            }

            xVocabList.add(jVocab)
        }
    }

    fun WriteMasterDataObject(md: IVocabularyElement): JObject {
        val jVocabElement = JObject(JProperty("id", md.id ?: ""), JProperty("attributes", JArray()))
        val jAttributes = jVocabElement["attributes"] as JArray?
            ?: throw Exception("Failed to grab attributes array.")

        val mappings = OTMappingTypeInformation.GetMasterDataXmlTypeInfo(md.javaClass)

        for (mapping in mappings.Properties) {
            val id = mapping.Name
            val p = mapping.Property

            val o = p.get(md)
            if (o != null) {
                if (id == "") {
                    val subMappings = OTMappingTypeInformation.GetMasterDataXmlTypeInfo(o.javaClass)
                    for (subMapping in subMappings.Properties) {
                        val subID = subMapping.Name
                        val subProperty = subMapping.Property
                        val subObj = subProperty.get(o)
                        if (subObj != null) {
                            if (subObj.javaClass == List::class.java) {
                                val l = subObj as List<LanguageString>
                                val str = l.firstOrNull()?.value
                                if (str != null) {
                                    val jAttribute = JObject(JProperty("id", subID), JProperty("attribute", str))
                                    jAttributes.add(jAttribute)
                                }
                            } else {
                                val str = subObj.toString()
                                if (str != null) {
                                    val jAttribute = JObject(JProperty("id", subID), JProperty("attribute", str))
                                    jAttributes.add(jAttribute)
                                }
                            }
                        }
                    }
                } else if (p.getAnnotation(OpenTraceabilityObjectAttribute::class.java) != null) {
                    val jAttribute = JObject(JProperty("id", id), JProperty("attribute", WriteObject(p.type, o)))
                    jAttributes.add(jAttribute)
                } else if (p.getAnnotation(OpenTraceabilityArrayAttribute::class.java) != null) {
                    val l = o as List<*>
                    for (i in l) {
                        val str = i.toString()
                        if (str != null) {
                            val jAttribute = JObject(JProperty("id", id), JProperty("attribute", str))
                            jAttributes.add(jAttribute)
                        }
                    }
                } else if (o.javaClass == List::class.java) {
                    val l = o as List<LanguageString>
                    val str = l.firstOrNull()?.value
                    if (str != null) {
                        val jAttribute = JObject(JProperty("id", id), JProperty("attribute", str))
                        jAttributes.add(jAttribute)
                    }
                } else {
                    val str = o.toString()
                    if (str != null) {
                        val jAttribute = JObject(JProperty("id", id), JProperty("attribute", str))
                        jAttributes.add(jAttribute)
                    }
                }
            }
        }

        for (kde in md.KDEs) {
            val jKDE = kde.getGS1WebVocabJson()
            if (jKDE != null) {
                val jAttribute = JObject(JProperty("id", kde.Name), JProperty("attribute", jKDE))
                jAttributes.add(jAttribute)
            }
        }

        return jVocabElement
    }

    fun writeObject(t: Class<*>, o: Any): JObject {
        val j = JObject()
        for (property in t.declaredFields) {
            val value = property.get(o)
            if (value != null) {
                val xmlAtt = property.getAnnotation(OpenTraceabilityAttribute::class.java)
                if (xmlAtt != null) {
                    if (property.getAnnotation(OpenTraceabilityObjectAttribute::class.java) != null) {
                        j[xmlAtt.name] = writeObject(property.type, value)
                    } else {
                        val str = value.toString()
                        if (str != null) {
                            j[xmlAtt.name] = str
                        }
                    }
                }
            }
        }
        return j
    }
}
