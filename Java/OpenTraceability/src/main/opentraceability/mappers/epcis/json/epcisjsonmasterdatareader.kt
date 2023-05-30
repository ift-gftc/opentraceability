package mappers.epcis.json

import com.fasterxml.jackson.core.JsonToken
import com.intellij.icons.AllIcons.Nodes.Models
import com.intellij.json.psi.JsonObject
import interfaces.IVocabularyElement
import mappers.OTMappingTypeInformation
import models.common.LanguageString
import models.events.*
import models.events.EPCISBaseDocument
import models.identifiers.PGLN
import models.masterdata.Tradeitem
import models.masterdata.TradingParty
import models.masterdata.VocabularyElement
import models.masterdata.kdes.MasterDataKDEObject
import org.json.simple.JSONObject
import utility.Countries
import utility.Country
import utility.attributes.OpenTraceabilityAttribute
import utility.attributes.OpenTraceabilityObjectAttribute
import java.lang.reflect.Type
import java.net.URI
import kotlin.reflect.KProperty

class EPCISJsonMasterDataReader {
    companion object {

        fun ReadMasterData(doc: EPCISBaseDocument, jMasterData: JSONObject) {
            jMasterData.optJSONArray("vocabularyList")?.let { jVocabList ->
                for (i in 0 until jVocabList.length()) {
                    val jVocabListItem = jVocabList.getJSONObject(i)
                    val type = jVocabListItem.optString("type", null)?.toLowerCase()
                    type?.let {
                        jVocabListItem.optJSONArray("vocabularyElementList")?.let { jVocabElementaryList ->
                            for (j in 0 until jVocabElementaryList.length()) {
                                val jVocabEle = jVocabElementaryList.getJSONObject(j)
                                when (type) {
                                    "urn:epcglobal:epcis:vtype:epcclass" -> ReadTradeItem(doc, jVocabEle, type)
                                    "urn:epcglobal:epcis:vtype:location" -> ReadLocation(doc, jVocabEle, type)
                                    "urn:epcglobal:epcis:vtype:party" -> ReadTradingParty(doc, jVocabEle, type)
                                    else -> ReadUnknown(doc, jVocabEle, type)
                                }
                            }
                        }
                    }
                }
            }
        }


        internal fun ReadTradeitem(doc: EPCISBaseDocument, xTradeitem: JsonObject, type: String) {
            TODO("Not yet implemented")
        }

        private fun readTradeItem(doc: EPCISBaseDocument, xTradeitem: JSONObject, type: String) {
            // read the GTIN from the id
            val id = xTradeitem.optString("id", "")
            val tradeitem = Tradeitem()
            tradeitem.GTIN = Models.Identifiers.GTIN(id)
            tradeitem.EPCISType = type

            // read the object
            ReadMasterDataObject(tradeitem, xTradeitem)
            doc.MasterData.add(tradeitem)
        }


        internal fun ReadLocation(doc: EPCISBaseDocument, xLocation: JsonObject, type: String) {
            TODO("Not yet implemented")
        }

        private fun ReadLocation(doc: EPCISBaseDocument, xLocation: JSONObject, type: String) {
            // read the GLN from the id
            val id = xLocation.optString("id", "")
            val t = Setup.MasterDataTypes[type]
            val instance = t?.let { clazz -> clazz.getDeclaredConstructor().newInstance() }
            if (instance is Location) {
                instance.GLN = Models.Identifiers.GLN(id)
                instance.EPCISType = type

                // read the object
                ReadMasterDataObject(instance, xLocation)
                doc.MasterData.add(instance)
            } else {
                throw Exception("Failed to activate instance Location of $t")
            }
        }



        private fun readTradingParty(doc: EPCISBaseDocument, xTradingParty: JSONObject, type: String) {
            // read the PGLN from the id
            val id = xTradingParty.optString("id", "")
            val tp = TradingParty()
            tp.PGLN = Models.Identifiers.PGLN(id)
            tp.EPCISType = type

            // read the object
            ReadMasterDataObject(tp, xTradingParty)
            doc.MasterData.add(tp)
        }


        private fun ReadUnknown(doc: EPCISBaseDocument, xVocabElement: JSONObject, type: String) {
            // read the ID from the id
            val id = xVocabElement.optString("id", "")
            val ele = VocabularyElement()
            ele.ID = id
            ele.EPCISType = type

            // read the object
            ReadMasterDataObject(ele, xVocabElement)
            doc.MasterData.add(ele)
        }

        private fun ReadMasterDataObject(md: IVocabularyElement, jMasterData: JSONObject, readKDEs: Boolean = true) {
            val mappedProperties = OTMappingTypeInformation.GetMasterDataXmlTypeInfo(md.javaClass)

            // work on expanded objects...
            // these are objects on the vocab element represented by one or more attributes in the EPCIS master data
            val ignoreAttributes = mutableListOf<String>()
            for (property in mappedProperties.Properties.filter { it.Name == "" }) {
                val subMappedProperties = OTMappingTypeInformation.GetMasterDataXmlTypeInfo(property.Property.type)
                var setAttribute = false
                val subObject = property.Property.type.getDeclaredConstructor().newInstance()
                if (subObject != null) {
                    val jAttArray = jMasterData.optJSONArray("attributes")
                    if (jAttArray != null) {
                        for (i in 0 until jAttArray.length()) {
                            val jAtt = jAttArray.getJSONObject(i)
                            val id = jAtt.optString("id", "")
                            val propMapping = subMappedProperties[id]
                            if (propMapping != null) {
                                if (!TrySetValueType(jAtt.optString("attribute", ""), propMapping.property, subObject)) {
                                    val value = ReadKDEObject(jAtt, propMapping.property.type)
                                    propMapping.property.set(subObject, value)
                                }
                                setAttribute = true
                                ignoreAttributes.add(id)
                            }
                        }
                        if (setAttribute) {
                            property.Property.set(md, subObject)
                        }
                    }
                }
            }

// go through each standard attribute...
            val jAttArray = jMasterData.optJSONArray("attributes")
            if (jAttArray != null) {
                for (i in 0 until jAttArray.length()) {
                    val jAtt = jAttArray.getJSONObject(i)
                    val id = jAtt.optString("id", "")

                    if (ignoreAttributes.contains(id)) {
                        continue
                    }

                    val propMapping = mappedProperties[id]
                    if (propMapping != null) {
                        if (!TrySetValueType(jAtt.optString("attribute", ""), propMapping.property, md)) {
                            val value = ReadKDEObject(jAtt, propMapping.property.type)
                            propMapping.property.set(md, value)
                        }
                    } else if (readKDEs) {
                        val jAttValue = jAtt.opt("attribute")
                        if (jAttValue != null) {
                            if (jAttValue is JSONObject) {
                                // serialize into object kde...
                                val kdeObject = MasterDataKDEObject("", id)
                                kdeObject.SetFromGS1WebVocabJson(jAttValue)
                                md.KDEs.add(kdeObject)
                            } else {
                                // serialize into string kde
                                val kdeString = MasterDataKDEString("", id)
                                kdeString.setFromGS1WebVocabJson(jAttValue)
                                md.KDEs.add(kdeString)
                            }
                        }
                    }
                }
            }
        }


        private fun ReadKDEObject(j: JToken, t: Type): Any {
            val value = t.getDeclaredConstructor().newInstance() ?: throw Exception("Failed to create instance of ${t.name}")

            if (value is List<*>) {
                val list = value as List<Any>
                if (j is JArray) {
                    for (xchild in j) {
                        val child = ReadKDEObject(xchild, t.genericTypeArguments[0])
                        list.add(child)
                    }
                }
            } else {
                // go through each property...
                for (p in t.DeclaredFields) {
                    val xmlAtt = p.getAnnotation(OpenTraceabilityAttribute::class.java)
                    if (xmlAtt != null) {
                        val x = j[xmlAtt.name]
                        if (x != null) {
                            val objAtt = p.getAnnotation(OpenTraceabilityObjectAttribute::class.java)
                            if (objAtt != null) {
                                val o = ReadKDEObject(x, p.type)
                            } else if (!TrySetValueType(x.toString(), p, value)) {
                                throw Exception("Failed to set value type while reading KDE object. property = ${p.name}, type = ${t.Name}, json = ${x.toString()}")
                            }
                        }
                    }
                }
            }

            return value
        }



        private fun trySetValueType(value: String, property: KProperty<*>, obj: Any): Boolean {
            when (property.returnType.classifier) {
                String::class -> {
                    property.setter.call(obj, value)
                    return true
                }
                List::class -> {
                    val current = property.getter.call(obj) as MutableList<String>?
                    if (current == null) {
                        val newList = mutableListOf<String>()
                        newList.add(value)
                        property.setter.call(obj, newList)
                    } else {
                        current.add(value)
                    }
                    return true
                }
                Boolean::class, Boolean::class.nullable -> {
                    val v = value.toBoolean()
                    property.setter.call(obj, v)
                    return true
                }
                Double::class, Double::class.nullable -> {
                    val v = value.toDouble()
                    property.setter.call(obj, v)
                    return true
                }
                URI::class -> {
                    val v = URI.create(value)
                    property.setter.call(obj, v)
                    return true
                }
                List::class -> {
                    val l = mutableListOf<LanguageString>()
                    l.add(LanguageString("en-US", value))
                    property.setter.call(obj, l)
                    return true
                }
                Country::class -> {
                    val v = Countries.Parse(value)
                    property.setter.call(obj, v)
                    return true
                }
                PGLN::class -> {
                    val v = PGLN(value)
                    property.setter.call(obj, v)
                    return true
                }
                else -> return false
            }
        }

    }
}