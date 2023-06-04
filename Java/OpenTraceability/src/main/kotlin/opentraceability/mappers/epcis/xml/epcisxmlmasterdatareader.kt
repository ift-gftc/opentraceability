package opentraceability.mappers.epcis.xml

import opentraceability.interfaces.IVocabularyElement
import opentraceability.mappers.OTMappingTypeInformation
import opentraceability.models.common.LanguageString
import opentraceability.models.events.*
import opentraceability.models.events.EPCISBaseDocument
import opentraceability.models.identifiers.PGLN
import opentraceability.models.masterdata.*
import opentraceability.models.masterdata.kdes.*
import opentraceability.utility.*
import opentraceability.utility.attributes.*
import java.lang.reflect.Type
import java.net.URI
import org.w3c.dom.Element
import kotlin.reflect.*
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.starProjectedType

class EPCISXmlMasterDataReader {
    companion object {

        fun ReadMasterData(doc: EPCISBaseDocument, xMasterData: Element) {
            val xVocabList = xMasterData.element("VocabularyList")
            if (xVocabList != null) {
                for (xVocab in xVocabList.elements()) {
                    val type = xVocab.getAttribute("type")?.toLowerCase()
                    if (type != null) {
                        val xVocabElementaryList = xVocab.element("VocabularyElementList")
                        if (xVocabElementaryList != null) {
                            for (xVocabElement in xVocabElementaryList.elements()) {
                                when (type) {
                                    "urn:epcglobal:epcis:vtype:epcclass" -> ReadTradeitem(doc, xVocabElement, type)
                                    "urn:epcglobal:epcis:vtype:location" -> ReadLocation(doc, xVocabElement, type)
                                    "urn:epcglobal:epcis:vtype:party" -> ReadTradingParty(doc, xVocabElement, type)
                                    else -> ReadUnknown(doc, xVocabElement, type)
                                }
                            }
                        }
                    }
                }
            }
        }

        fun ReadTradeitem(doc: EPCISBaseDocument, xTradeitem: Element, type: String) {
            // read the GTIN from the id
            val id = xTradeitem.getAttribute("id") ?: ""
            val tradeitem = TradeItem()
            tradeitem.gtin = opentraceability.models.identifiers.GTIN(id)
            tradeitem.epcisType = type

            // read the object
            ReadMasterDataObject(tradeitem, xTradeitem)
            doc.masterData.add(tradeitem)
        }


        fun ReadLocation(doc: EPCISBaseDocument, xLocation: Element, type: String) {
            // read the GLN from the id
            val id = xLocation.getAttribute("id") ?: ""

            val t = opentraceability.Setup.MasterDataTypes[type]
            var loc = (t as KClass<Location>).createInstance()

            loc.gln = opentraceability.models.identifiers.GLN(id)
            loc.epcisType = type

            // read the object
            ReadMasterDataObject(loc, xLocation)
            doc.masterData.add(loc)
        }


        fun ReadTradingParty(doc: EPCISBaseDocument, xTradingParty: Element, type: String) {
            // read the PGLN from the id
            val id = xTradingParty.getAttribute("id") ?: ""
            val tp = TradingParty()
            tp.pgln = opentraceability.models.identifiers.PGLN(id)
            tp.epcisType = type

            // read the object
            ReadMasterDataObject(tp, xTradingParty)
            doc.masterData.add(tp)
        }


        fun ReadUnknown(doc: EPCISBaseDocument, xVocabElement: Element, type: String) {
            // read the PGLN from the id
            val id = xVocabElement.getAttribute("id") ?: ""
            val ele = VocabularyElement()
            ele.id = id
            ele.epcisType = type

            // read the object
            ReadMasterDataObject(ele, xVocabElement)
            doc.masterData.add(ele)
        }



        fun ReadMasterDataObject(md: IVocabularyElement, xMasterData: Element, readKDEs: Boolean = true) {
            val mappedProperties = OTMappingTypeInformation.getMasterDataXmlTypeInfo(md::class.starProjectedType as KClass<*>)

            // work on expanded objects...
            // these are objects on the vocab element represented by one or more attributes in the EPCIS master data
            val ignoreAttributes = mutableListOf<String>()
            for (property in mappedProperties.properties.filter { it.Name == "" }) {
                val subMappedProperties = OTMappingTypeInformation.getMasterDataXmlTypeInfo(property.Property.returnType as KClass<*>)
                var setAttribute = false
                val subObject = (property.Property.returnType as KClass<*>).createInstance()
                if (subObject != null) {
                    for (xeAtt in xMasterData.elements("attribute")) {
                        val id = xeAtt.getAttribute("id") ?: ""
                        val propMapping = subMappedProperties[id]
                        if (propMapping != null) {
                            if (!TrySetValueType(xeAtt.nodeValue, propMapping.Property, subObject)) {
                                val value = readKDEObject(xeAtt, propMapping.Property.returnType as KClass<*>)
                                propMapping.Property?.setter?.call(subObject, value)
                            }
                            setAttribute = true
                            ignoreAttributes.add(id)
                        }
                    }
                    if (setAttribute) {
                        property.Property?.setter?.call(md, subObject)
                    }
                }
            }

            // go through each standard attribute...
            for (xeAtt in xMasterData.elements("attribute")) {
                val id = xeAtt.getAttribute("id") ?: ""

                if (ignoreAttributes.contains(id)) {
                    continue
                }

                val propMapping = mappedProperties[id]
                if (propMapping != null) {
                    if (!TrySetValueType(xeAtt.nodeValue, propMapping.Property, md)) {
                        val value = readKDEObject(xeAtt, propMapping.Property.returnType as KClass<*>)
                        propMapping.Property?.setter?.call(md, value)
                    }
                } else if (readKDEs) {
                    if (xeAtt.hasChildNodes()) {
                        // serialize into object kde...
                        val kdeObject = MasterDataKDEObject("", id)
                        kdeObject.setFromEPCISXml(xeAtt)
                        md.kdes.add(kdeObject)
                    } else {
                        // serialize into string kde
                        val kdeString = MasterDataKDEString("", id)
                        kdeString.setFromEPCISXml(xeAtt)
                        md.kdes.add(kdeString)
                    }
                }
            }
        }


        fun readKDEObject(xeAtt: Element, t: KClass<*>): Any {
            val value = t.createInstance() ?: throw Exception("Failed to create instance of ${t.qualifiedName}")

            if (value is MutableList<*>) {
                val list = value as MutableList<Any>
                for (xchild in xeAtt.elements()) {
                    val child = readKDEObject(xchild, t.typeParameters[0].starProjectedType as KClass<*>)
                    list.add(child)
                }
            } else {
                // go through each property...
                for (p in t.memberProperties) {
                    val xmlAtt = p.annotations.filterIsInstance<OpenTraceabilityAttribute>().firstOrNull()
                    if (xmlAtt != null) {
                        val x = xeAtt.element(xmlAtt.name)
                        if (x != null) {
                            val objAtt = p.annotations.filterIsInstance<OpenTraceabilityObjectAttribute>().firstOrNull()
                            if (objAtt != null) {
                                val o = readKDEObject(x, p.returnType as KClass<*>)
                            } else if (!TrySetValueType(x.nodeValue, p as KMutableProperty<*>, value)) {
                                throw Exception("Failed to set value type while reading KDE object. property = ${p.name}, type = ${t.qualifiedName}, xml = ${x.toString()}")
                            }
                        }
                    }
                }
            }

            return value
        }


        fun TrySetValueType(`val`: String, p: KMutableProperty<*>, o: Any): Boolean {
            when {
                p.returnType == String::class.java -> {
                    p.setter.call(o, `val`)
                    return true
                }
                p.returnType == MutableList::class.java && p.returnType == List::class.java -> {
                    val cur = p.getter.call(o) as MutableList<String>?
                    if (cur == null) {
                        val newList = mutableListOf<String>()
                        newList.add(`val`)
                        p.setter.call(o, newList)
                    } else {
                        cur.add(`val`)
                    }
                    return true
                }
                p.returnType == Boolean::class.java || p.returnType == java.lang.Boolean::class.java -> {
                    val v = java.lang.Boolean.parseBoolean(`val`)
                    p.setter.call(o, v)
                    return true
                }
                p.returnType == Double::class.java || p.returnType == java.lang.Double::class.java -> {
                    val v = java.lang.Double.parseDouble(`val`)
                    p.setter.call(o, v)
                    return true
                }
                p.returnType == URI::class.java -> {
                    val v = URI.create(`val`)
                    p.setter.call(o, v)
                    return true
                }
                p.typeParameters[0].starProjectedType == typeOf<MutableList<LanguageString>>() -> {
                    val l = mutableListOf<LanguageString>()
                    l.add(LanguageString("en-US", `val`))
                    p.setter.call(o, l)
                    return true
                }
                p.returnType == Country::class.java -> {
                    val v = Countries.parse(`val`)
                    p.setter.call(o, v)
                    return true
                }
                p.returnType == PGLN::class.java -> {
                    val v = PGLN(`val`)
                    p.setter.call(o, v)
                    return true
                }
                else -> return false
            }
        }

    }
}
