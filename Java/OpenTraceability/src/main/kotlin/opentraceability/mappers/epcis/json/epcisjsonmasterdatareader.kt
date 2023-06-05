package opentraceability.mappers.epcis.json

import opentraceability.interfaces.IVocabularyElement
import opentraceability.mappers.OTMappingTypeInformation
import opentraceability.models.common.LanguageString
import opentraceability.models.events.*
import opentraceability.models.identifiers.PGLN
import opentraceability.models.masterdata.*
import opentraceability.models.masterdata.kdes.*
import opentraceability.utility.*
import opentraceability.utility.attributes.*
import java.net.URI
import kotlin.reflect.KMutableProperty
import org.json.JSONArray
import org.json.JSONObject
import kotlin.reflect.KClass
import kotlin.reflect.full.*
import kotlin.reflect.typeOf

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
            val tradeitem = TradeItem()
            tradeitem.gtin = opentraceability.models.identifiers.GTIN(id)
            tradeitem.epcisType = type

            // read the object
            readMasterDataObject(tradeitem as IVocabularyElement, xTradeitem)
            doc.masterData.add(tradeitem)
        }

        fun readLocation(doc: EPCISBaseDocument, xLocation: JSONObject, type: String) {
            // read the GLN from the id
            val id = xLocation.optString("id", "")
            val t = opentraceability.Setup.MasterDataTypes[type]
            val loc = (t!!::class.createInstance() as? Location) ?: throw Exception("Failed to activate instance Location of $t")

            loc.gln = opentraceability.models.identifiers.GLN(id)
            loc.epcisType = type

            // read the object
            readMasterDataObject(loc, xLocation)
            doc.masterData.add(loc)
        }

        fun readTradingParty(doc: EPCISBaseDocument, xTradingParty: JSONObject, type: String) {
            // read the PGLN from the id
            val id = xTradingParty.optString("id", "")
            val tp = TradingParty()
            tp.pgln = opentraceability.models.identifiers.PGLN(id)
            tp.epcisType = type

            // read the object
            readMasterDataObject(tp as IVocabularyElement, xTradingParty)
            doc.masterData.add(tp)
        }

        fun ReadUnknown(doc: EPCISBaseDocument, xVocabElement: JSONObject, type: String) {
            // read the ID from the id
            val id = xVocabElement.optString("id", "")
            val ele = VocabularyElement() as IVocabularyElement
            ele.id = id
            ele.epcisType = type

            // read the object
            readMasterDataObject(ele, xVocabElement)
            doc.masterData.add(ele)
        }

        fun readMasterDataObject(md: IVocabularyElement, jMasterData: JSONObject, readKDEs: Boolean = true) {
            val mappedProperties = OTMappingTypeInformation.getMasterDataXmlTypeInfo(md::class.starProjectedType as KClass<*>)

            // work on expanded objects...
            // these are objects on the vocab element represented by one or more attributes in the EPCIS master data
            val ignoreAttributes = mutableListOf<String>()
            for (property in mappedProperties.properties.filter { it.Name == "" })
            {
                val subMappedProperties = OTMappingTypeInformation.getMasterDataXmlTypeInfo(property.Property.returnType as KClass<*>)
                var setAttribute = false
                val subObject = (property.Property.returnType as KClass<*>).createInstance()
                if (subObject != null) {
                    val jAttArray = jMasterData.optJSONArray("attributes")
                    if (jAttArray != null) {
                        for (i in 0 until jAttArray.length()) {
                            val jAtt = jAttArray.getJSONObject(i)
                            val id = jAtt.optString("id", "")
                            val propMapping = subMappedProperties[id]
                            if (propMapping != null)
                            {
                                if (!trySetValueType(jAtt.optString("attribute", ""), propMapping.Property, subObject)) {
                                    val value = readKDEObject(jAtt, propMapping.Property.returnType as KClass<*>)
                                    propMapping.Property.setter.call(subObject, value)
                                }
                                setAttribute = true
                                ignoreAttributes.add(id)
                            }
                        }
                        if (setAttribute) {
                            property.Property.setter.call(md, subObject)
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
                            val value = readKDEObject(jAtt, propMapping.Property.returnType as KClass<*>)
                            propMapping.Property.setter.call(md, value)
                        }
                    } else if (readKDEs) {
                        val jAttValue = jAtt.opt("attribute")
                        if (jAttValue != null) {
                            if (jAttValue is JSONObject) {
                                // serialize into object kde...
                                val kdeObject = MasterDataKDEObject("", id)
                                kdeObject.setFromGS1WebVocabJson(jAttValue)
                                md.kdes.add(kdeObject)
                            } else {
                                // serialize into string kde
                                val kdeString = MasterDataKDEString("", id)
                                kdeString.value = jAttValue.toString()
                                md.kdes.add(kdeString)
                            }
                        }
                    }
                }
            }
        }

        fun readKDEObject(j: JSONObject, t: KClass<*>): Any {
            var value = t.createInstance()

            if (value is MutableList<*>) {
                val list = value as MutableList<Any>
                if (j is JSONArray)
                {
                    for (xchild in j) {
                        val child = readKDEObject(xchild as JSONObject, t.typeParameters.first().starProjectedType as KClass<*>)
                        list.add(child)
                    }
                }
            }
            else
            {
                // go through each property...
                for (p in t.memberProperties) {
                    var kprop = p as KMutableProperty<*>
                    if (kprop != null)
                    {
                        val xmlAtt = kprop.annotations.filterIsInstance<OpenTraceabilityAttribute>().firstOrNull()
                        if (xmlAtt != null) {
                            val x = j[xmlAtt.name]
                            if (x != null) {
                                val objAtt = kprop.annotations.filterIsInstance<OpenTraceabilityObjectAttribute>().firstOrNull()
                                if (objAtt != null)
                                {
                                    val o = readKDEObject(x as JSONObject, kprop.returnType as KClass<*>)
                                    kprop.setter.call(value, o)
                                }
                                else if (!trySetValueType(x.toString(), kprop, value)) {
                                    throw Exception("Failed to set value type while reading KDE object. property = ${kprop.name}, type = ${t.simpleName}, json = ${x.toString()}")
                                }
                            }
                        }
                    }
                }
            }

            return value
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
                returnType == typeOf<MutableList<LanguageString>>() -> {
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