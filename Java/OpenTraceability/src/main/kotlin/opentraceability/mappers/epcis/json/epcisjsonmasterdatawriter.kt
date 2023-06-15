package opentraceability.mappers.epcis.json

import opentraceability.interfaces.IVocabularyElement
import opentraceability.mappers.OTMappingTypeInformation
import opentraceability.models.common.LanguageString
import opentraceability.models.events.*
import org.json.*
import opentraceability.utility.attributes.*
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.typeOf

object EPCISJsonMasterDataWriter {
    fun writeMasterData(jDoc: JSONObject, doc: EPCISBaseDocument) {
        if (doc.masterData.isNotEmpty()) {
            val xEPCISHeader = jDoc.optJSONObject("epcisHeader") ?: JSONObject()
            val epcisMasterData = xEPCISHeader.optJSONObject("epcisMasterData") ?: JSONObject()
            val vocabularyList = epcisMasterData.optJSONArray("vocabularyList") ?: JSONArray()

            for ((type, mdList) in doc.masterData.groupBy { it.epcisType }) {
                if (type != null) {
                    val jVocabList = writeMasterDataList(mdList, type)
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

    private fun writeMasterDataList(data: List<IVocabularyElement>, type: String): JSONObject {
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

        val mappings = OTMappingTypeInformation.getMasterDataXmlTypeInfo(md::class.starProjectedType as KClass<*>)

        for (mapping in mappings.properties) {
            val id = mapping.Name
            val p = mapping.Property

            val o = p.getter.call(md)
            if (o != null) {
                if (id.isBlank()) {
                    val subMappings = OTMappingTypeInformation.getMasterDataXmlTypeInfo(o::class.starProjectedType as KClass<*>)
                    for (subMapping in subMappings.properties) {
                        val subID = subMapping.Name
                        val subProperty = subMapping.Property
                        val subObj = subProperty.getter.call(o)
                        if (subObj != null) {
                            if (subObj is ArrayList<*>) {
                                val l = subObj as ArrayList<LanguageString>
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
                } else if (p.annotations.filterIsInstance<OpenTraceabilityObjectAttribute>().isNotEmpty()) {
                    val jAttribute = JSONObject()
                    jAttribute.put("id", id)
                    jAttribute.put("attribute", writeObject(p.returnType as KClass<*>, o))
                    attributes.put(jAttribute)
                } else if (p.annotations.filterIsInstance<OpenTraceabilityArrayAttribute>().isNotEmpty()) {
                    val l = o as ArrayList<*>
                    for (i in l) {
                        val str = i.toString()
                        if (str.isNotBlank()) {
                            val jAttribute = JSONObject()
                            jAttribute.put("id", id)
                            jAttribute.put("attribute", str)
                            attributes.put(jAttribute)
                        }
                    }
                }
                else if (o::class.starProjectedType == typeOf<ArrayList<LanguageString>>())
                {
                    val l = o as ArrayList<LanguageString>
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

        for (kde in md.kdes) {
            val jKDE = kde.getGS1WebVocabJson()
            if (jKDE != null) {
                val jAttribute = JSONObject()
                jAttribute.put("id", kde.name)
                jAttribute.put("attribute", jKDE)
                attributes.put(jAttribute)
            }
        }

        jVocabElement.put("id", md.id ?: "")
        jVocabElement.put("attributes", attributes)

        return jVocabElement
    }

    private fun writeObject(t: KClass<*>, o: Any): JSONObject {
        val j = JSONObject()
        for (property in t.memberProperties) {
            val value = property.getter.call(o)
            if (value != null) {
                val xmlAtt = property.annotations.filterIsInstance<OpenTraceabilityAttribute>().firstOrNull()
                if (xmlAtt != null) {
                    if (property.annotations.filterIsInstance<OpenTraceabilityObjectAttribute>().isNotEmpty())
                    {
                        j.put(xmlAtt.name, writeObject(property.returnType as KClass<*>, value))
                    }
                    else
                    {
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

