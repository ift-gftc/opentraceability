package mappers.epcis.json

import interfaces.IVocabularyElement
import mappers.OTMappingTypeInformation
import models.common.LanguageString
import models.events.*
import models.identifiers.PGLN
import models.masterdata.*
import models.masterdata.kdes.*
import org.json.*
import utility.*
import utility.attributes.*
import java.lang.reflect.Type
import java.net.URI
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.withNullability
import kotlin.reflect.full.createType
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.ParameterizedType

class EPCISJsonMasterDataReader {
    companion object {

        fun readMasterData(doc: EPCISBaseDocument, jMasterData: JSONObject) {
            jMasterData.optJSONArray("vocabularyList")?.let { jVocabList ->
                for (i in 0 until jVocabList.length()) {
                    val jVocabListItem = jVocabList.getJSONObject(i)
                    val type = jVocabListItem.optString("type", null)?.toLowerCase()
                    type?.let {
                        jVocabListItem.optJSONArray("vocabularyElementList")?.let { jVocabElementaryList ->
                            for (j in 0 until jVocabElementaryList.length()) {
                                val jVocabEle = jVocabElementaryList.getJSONObject(j)
                                when (type) {
                                    "urn:epcglobal:epcis:vtype:epcclass" -> readTradeItem(doc, jVocabEle, type)
                                    "urn:epcglobal:epcis:vtype:location" -> readLocation(doc, jVocabEle, type)
                                    "urn:epcglobal:epcis:vtype:party" -> readTradingParty(doc, jVocabEle, type)
                                    else -> ReadUnknown(doc, jVocabEle, type)
                                }
                            }
                        }
                    }
                }
            }
        }

        fun readTradeItem(doc: EPCISBaseDocument, xTradeitem: JSONObject, type: String) {
            // read the GTIN from the id
            val id = xTradeitem.optString("id", "")
            val tradeitem = Tradeitem()
            tradeitem.GTIN = models.identifiers.GTIN(id)
            tradeitem.EPCISType = type

            // read the object
            readMasterDataObject(tradeitem as IVocabularyElement, xTradeitem)
            doc.MasterData.add(tradeitem)
        }

        fun readLocation(doc: EPCISBaseDocument, xLocation: JSONObject, type: String) {
            // read the GLN from the id
            val id = xLocation.optString("id", "")
            val t = Setup.MasterDataTypes[type]
            val loc = (t!!::class.createInstance() as? Location) ?: throw Exception("Failed to activate instance Location of $t")

            loc.GLN = models.identifiers.GLN(id)
            loc.EPCISType = type

            // read the object
            readMasterDataObject(loc, xLocation)
            doc.MasterData.add(loc)
        }




        fun readTradingParty(doc: EPCISBaseDocument, xTradingParty: JSONObject, type: String) {
            // read the PGLN from the id
            val id = xTradingParty.optString("id", "")
            val tp = TradingParty()
            tp.PGLN = models.identifiers.PGLN(id)
            tp.EPCISType = type

            // read the object
            readMasterDataObject(tp as IVocabularyElement, xTradingParty)
            doc.MasterData.add(tp)
        }


        fun ReadUnknown(doc: EPCISBaseDocument, xVocabElement: JSONObject, type: String) {
            // read the ID from the id
            val id = xVocabElement.optString("id", "")
            val ele = VocabularyElement() as IVocabularyElement
            ele.ID = id
            ele.EPCISType = type

            // read the object
            readMasterDataObject(ele, xVocabElement)
            doc.MasterData.add(ele)
        }

        fun readMasterDataObject(md: IVocabularyElement, jMasterData: JSONObject, readKDEs: Boolean = true) {
            val mappedProperties = OTMappingTypeInformation.getMasterDataXmlTypeInfo(md.javaClass)

            // work on expanded objects...
            // these are objects on the vocab element represented by one or more attributes in the EPCIS master data
            val ignoreAttributes = mutableListOf<String>()
            for (property in mappedProperties.properties.filter { it.Name == "" }) {
                val subMappedProperties = OTMappingTypeInformation.getMasterDataXmlTypeInfo(property.Property.type)
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
                                if (!trySetValueType(jAtt.optString("attribute", ""), propMapping.Property, subObject)) {
                                    val value = readKDEObject(jAtt, propMapping.Property.type)
                                    propMapping.Property.set(subObject, value)
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
                        if (!trySetValueType(jAtt.optString("attribute", ""), propMapping.Property, md)) {
                            val value = readKDEObject(jAtt, propMapping.Property.type)
                            propMapping.Property.set(md, value)
                        }
                    } else if (readKDEs) {
                        val jAttValue = jAtt.opt("attribute")
                        if (jAttValue != null) {
                            if (jAttValue is JSONObject) {
                                // serialize into object kde...
                                val kdeObject = MasterDataKDEObject("", id)
                                kdeObject.setFromGS1WebVocabJson(jAttValue)
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


        fun readKDEObject(j: JSONObject, t: Type): Any {
            val value = (t as Class<*>).getDeclaredConstructor().newInstance() ?: throw Exception("Failed to create instance of ${t.name}")

            if (value is List<*>) {
                val list = value as MutableList<Any>
                if (j is JSONArray) {
                    for (xchild in j) {
                        val child = readKDEObject(xchild as JSONObject, getListGenericType(t))
                        list.add(child)
                    }
                }
            } else {
                // go through each property...
                for (p in t.declaredFields) {
                    val xmlAtt = p.getAnnotation(OpenTraceabilityAttribute::class.java)
                    if (xmlAtt != null) {
                        val x = j[xmlAtt.name]
                        if (x != null) {
                            val objAtt = p.getAnnotation(OpenTraceabilityObjectAttribute::class.java)
                            if (objAtt != null) {
                                val o = readKDEObject(x as JSONObject, p.type)
                                p.isAccessible = true
                                p.set(value, o)
                            } else if (!trySetValueType(x.toString(), p, value)) {
                                throw Exception("Failed to set value type while reading KDE object. property = ${p.name}, type = ${t.simpleName}, json = ${x.toString()}")
                            }
                        }
                    }
                }
            }

            return value
        }

        private fun getListGenericType(type: Type): Type {
            if (type is ParameterizedType) {
                val typeArgs = type.actualTypeArguments
                if (typeArgs.isNotEmpty()) {
                    return typeArgs[0]
                }
            }
            throw IllegalArgumentException("Failed to get generic type argument for List")
        }


        fun trySetValueType(value: String, property: KMutableProperty<*>, obj: Any): Boolean {
            val returnType = property.returnType

            when {
                returnType.isSubtypeOf(String::class.starProjectedType) -> {
                    property.setter.call(obj, value)
                    return true
                }
                returnType.isSubtypeOf(List::class.starProjectedType) -> {
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
                returnType.isSubtypeOf(Boolean::class.starProjectedType.withNullability(true)) -> {
                    val v = value.toBoolean()
                    property.setter.call(obj, v)
                    return true
                }
                returnType.isSubtypeOf(Double::class.starProjectedType.withNullability(true)) -> {
                    val v = value.toDouble()
                    property.setter.call(obj, v)
                    return true
                }
                returnType.isSubtypeOf(URI::class.starProjectedType) -> {
                    val v = URI.create(value)
                    property.setter.call(obj, v)
                    return true
                }
                returnType.isSubtypeOf(List::class.createType(listOf(LanguageString::class.starProjectedType))) -> {
                    val l = mutableListOf<LanguageString>()
                    l.add(LanguageString("en-US", value))
                    property.setter.call(obj, l)
                    return true
                }
                returnType.isSubtypeOf(Country::class.starProjectedType) -> {
                    val v = Countries.parse(value)
                    property.setter.call(obj, v)
                    return true
                }
                returnType.isSubtypeOf(PGLN::class.starProjectedType) -> {
                    val v = PGLN(value)
                    property.setter.call(obj, v)
                    return true
                }
                else -> return false
            }
        }
    }
}