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
            val xEPCISHeader = jDoc.optJSONObject("epcisHeader") ?: JSONObject()
            val epcisMasterData = xEPCISHeader.optJSONObject("epcisMasterData") ?: JSONObject()
            val vocabularyList = epcisMasterData.optJSONArray("vocabularyList") ?: JSONArray()

            for ((type, mdList) in doc.MasterData.groupBy { it.EPCISType }) {
                if (type != null) {
                    val jVocabList = writeMasterDataList(mdList.toMutableList(), type)
                    vocabularyList.put(jVocabList)
                } else {
                    throw Exception("There are master data vocabulary elements where the Type is NULL.")
                }
            }

            epcisMasterData.put("vocabularyList", vocabularyList)
            xEPCISHeader.put("epcisMasterData", epcisMasterData)
            jDoc.put("epcisHeader", xEPCISHeader)
        }
    }

    private fun writeMasterDataList(data: MutableList<IVocabularyElement>, type: String): JSONObject {
        val jVocab = JSONObject()
        val vocabularyElementList = JSONArray()

        for (md in data) {
            val xMD = writeMasterDataObject(md)
            vocabularyElementList.put(xMD)
        }

        jVocab.put("type", type)
        jVocab.put("vocabularyElementList", vocabularyElementList)

        return jVocab
    }

    private fun writeMasterDataObject(md: IVocabularyElement): JSONObject {
        val jVocabElement = JSONObject()
        val attributes = JSONArray()

        val mappings = OTMappingTypeInformation.getMasterDataXmlTypeInfo(md.javaClass)

        for (mapping in mappings.properties) {
            val id = mapping.Name
            val p = mapping.Property

            val o = p.get(md)
            if (o != null) {
                if (id.isBlank()) {
                    val subMappings = OTMappingTypeInformation.getMasterDataXmlTypeInfo(o.javaClass)
                    for (subMapping in subMappings.properties) {
                        val subID = subMapping.Name
                        val subProperty = subMapping.Property
                        val subObj = subProperty.get(o)
                        if (subObj != null) {
                            if (subObj is List<*>) {
                                val l = subObj as List<LanguageString>
                                val str = l.firstOrNull()?.value
                                if (str != null) {
                                    val jAttribute = JSONObject()
                                    jAttribute.put("id", subID)
                                    jAttribute.put("attribute", str)
                                    attributes.put(jAttribute)
                                }
                            } else {
                                val str: String = subObj.toString()
                                if (str.isNotBlank()) {
                                    val jAttribute = JSONObject()
                                    jAttribute.put("id", subID)
                                    jAttribute.put("attribute", str)
                                    attributes.put(jAttribute)
                                }
                            }
                        }
                    }
                } else if (p.getAnnotation(OpenTraceabilityObjectAttribute::class.java) != null) {
                    val jAttribute = JSONObject()
                    jAttribute.put("id", id)
                    jAttribute.put("attribute", writeObject(p.type, o))
                    attributes.put(jAttribute)
                } else if (p.getAnnotation(OpenTraceabilityArrayAttribute::class.java) != null) {
                    val l = o as List<*>
                    for (i in l) {
                        val str = i.toString()
                        if (str.isNotBlank()) {
                            val jAttribute = JSONObject()
                            jAttribute.put("id", id)
                            jAttribute.put("attribute", str)
                            attributes.put(jAttribute)
                        }
                    }
                } else if (o is List<*>) {
                    val l = o as List<LanguageString>
                    val str = l.firstOrNull()?.value
                    if (str != null) {
                        val jAttribute = JSONObject()
                        jAttribute.put("id", id)
                        jAttribute.put("attribute", str)
                        attributes.put(jAttribute)
                    }
                } else {
                    val str: String = o.toString()
                    if (str.isNotBlank()) {
                        val jAttribute = JSONObject()
                        jAttribute.put("id", id)
                        jAttribute.put("attribute", str)
                        attributes.put(jAttribute)
                    }
                }
            }
        }

        for (kde in md.KDEs) {
            val jKDE = kde.GetGS1WebVocabJson()
            if (jKDE != null) {
                val jAttribute = JSONObject()
                jAttribute.put("id", kde.Name)
                jAttribute.put("attribute", jKDE)
                attributes.put(jAttribute)
            }
        }

        jVocabElement.put("id", md.ID ?: "")
        jVocabElement.put("attributes", attributes)

        return jVocabElement
    }

    private fun writeObject(t: Class<*>, o: Any): JSONObject {
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
                        if (str.isNotBlank()) {
                            j.put(xmlAtt.name, str)
                        }
                    }
                }
            }
        }
        return j
    }
}

