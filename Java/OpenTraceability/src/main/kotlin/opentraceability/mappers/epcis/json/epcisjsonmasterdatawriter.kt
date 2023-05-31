package mappers.epcis.json

import com.fasterxml.jackson.annotation.JsonProperty
import interfaces.IVocabularyElement
import mappers.OTMappingTypeInformation
import models.common.LanguageString
import models.events.*
import org.json.*
import utility.attributes.*


object EPCISJsonMasterDataWriter {
    fun writeMasterData(jDoc: JSONObject, doc: EPCISBaseDocument) {
        if (doc.MasterData.isNotEmpty()) {
            val xEPCISHeader = jDoc["epcisHeader"] as JSONObject?
            if (xEPCISHeader == null) {

                jDoc.put(
                    "epcisHeader",
                    JSONObject(JsonProperty("epcisMasterData", JSONObject(JsonProperty("vocabularyList", JSONArray()))))
                )


            } else {
                xEPCISHeader.put("epcisMasterData", JSONObject(JsonProperty("vocabularyList", JSONArray())))
            }
            val jVocabList = jDoc["epcisHeader"]?.get("epcisMasterData")?.get("vocabularyList") as JSONArray?
                ?: throw Exception("Failed to grab json object epcisHeader.epcisMasterData.vocabularyList")

            for ((type, mdList) in doc.MasterData.groupBy { it.EPCISType }) {
                if (type != null) {
                    writeMasterDataList(mdList.toMutableList(), jVocabList, type)
                } else {
                    throw Exception("There are master data vocabulary elements where the Type is NULL.")
                }
            }
        }
    }

    fun writeMasterDataList(data: MutableList<IVocabularyElement>, xVocabList: JSONArray, type: String) {
        if (data.isNotEmpty()) {
            val jVocab = JSONObject(JsonProperty("type", type), JsonProperty("vocabularyElementList", JSONArray()))
            val xVocabEleList = jVocab["vocabularyElementList"] as JSONArray?
                ?: throw Exception("Failed to grab the array vocabularyElementList")

            for (md in data) {
                val xMD = writeMasterDataObject(md)
                xVocabEleList.put(xMD)
            }

            xVocabList.put(jVocab)
        }
    }

    fun writeMasterDataObject(md: IVocabularyElement): JSONObject {
        val jVocabElement = JSONObject(JsonProperty("id", md.ID ?: ""), JsonProperty("attributes", JSONArray()))
        val jAttributes = jVocabElement["attributes"] as JSONArray?
            ?: throw Exception("Failed to grab attributes array.")

        val mappings = OTMappingTypeInformation.getMasterDataXmlTypeInfo(md.javaClass)

        for (mapping in mappings.properties) {
            val id = mapping.Name
            val p = mapping.Property

            val o = p.get(md)
            if (o != null) {
                if (id == "") {
                    val subMappings = OTMappingTypeInformation.getMasterDataXmlTypeInfo(o.javaClass)
                    for (subMapping in subMappings.properties) {
                        val subID = subMapping.Name
                        val subProperty = subMapping.Property
                        val subObj = subProperty.get(o)
                        if (subObj != null) {
                            if (subObj.javaClass == List::class.java) {
                                val l = subObj as List<LanguageString>
                                val str = l.firstOrNull()?.value
                                if (str != null) {
                                    val jAttribute =
                                        JSONObject(JsonProperty("id", subID), JsonProperty("attribute", str))
                                    jAttributes.put(jAttribute)
                                }
                            } else {
                                val str = subObj.toString()
                                if (str != null) {
                                    val jAttribute =
                                        JSONObject(JsonProperty("id", subID), JsonProperty("attribute", str))
                                    jAttributes.put(jAttribute)
                                }
                            }
                        }
                    }
                } else if (p.getAnnotation(OpenTraceabilityObjectAttribute::class.java) != null) {
                    val jAttribute =
                        JSONObject(JsonProperty("id", id), JsonProperty("attribute", writeObject(p.type, o)))
                    jAttributes.put(jAttribute)
                } else if (p.getAnnotation(OpenTraceabilityArrayAttribute::class.java) != null) {
                    val l = o as List<*>
                    for (i in l) {
                        val str = i.toString()
                        if (str != null) {
                            val jAttribute = JSONObject(JsonProperty("id", id), JsonProperty("attribute", str))
                            jAttributes.put(jAttribute)
                        }
                    }
                } else if (o.javaClass == List::class.java) {
                    val l = o as List<LanguageString>
                    val str = l.firstOrNull()?.value
                    if (str != null) {
                        val jAttribute = JSONObject(JsonProperty("id", id), JsonProperty("attribute", str))
                        jAttributes.put(jAttribute)
                    }
                } else {
                    val str = o.toString()
                    if (str != null) {
                        val jAttribute = JSONObject(JsonProperty("id", id), JsonProperty("attribute", str))
                        jAttributes.put(jAttribute)
                    }
                }
            }
        }

        for (kde in md.KDEs) {
            val jKDE = kde.getGS1WebVocabJson()
            if (jKDE != null) {
                val jAttribute = JSONObject(JsonProperty("id", kde.name), JsonProperty("attribute", jKDE))
                jAttributes.put(jAttribute)
            }
        }

        return jVocabElement
    }

    fun writeObject(t: Class<*>, o: Any): JSONObject {
        val j = JSONObject()
        for (property in t.declaredFields) {
            val value = property.get(o)
            if (value != null) {
                val xmlAtt = property.getAnnotation(OpenTraceabilityAttribute::class.java)
                if (xmlAtt != null) {
                    if (property.getAnnotation(OpenTraceabilityObjectAttribute::class.java) != null) {
                        j.put(xmlAtt.name, writeObject(property.type, value))
                    } else {
                        val str = value.toString()
                        if (str != null) {
                            j.put(xmlAtt.name, str)
                        }
                    }
                }
            }
        }
        return j
    }
}
