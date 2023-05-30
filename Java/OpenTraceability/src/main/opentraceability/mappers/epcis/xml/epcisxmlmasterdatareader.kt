package mappers.epcis.xml

import interfaces.IVocabularyElement
import mappers.OTMappingTypeInformation
import models.common.LanguageString
import javax.xml.bind.annotation.*
import models.events.*
import models.events.EPCISBaseDocument
import models.identifiers.PGLN
import models.masterdata.TradingParty
import models.masterdata.VocabularyElement
import models.masterdata.kdes.MasterDataKDEObject
import models.masterdata.kdes.MasterDataKDEString
import utility.Countries
import utility.Country
import utility.attributes.OpenTraceabilityAttribute
import utility.attributes.OpenTraceabilityObjectAttribute
import java.lang.reflect.Type
import java.net.URI

class EPCISXmlMasterDataReader {
    companion object {

        fun ReadMasterData(doc: EPCISBaseDocument, xMasterData: XElement) {
            val xVocabList = xMasterData.element("VocabularyList")
            if (xVocabList != null) {
                for (xVocab in xVocabList.elements()) {
                    val type = xVocab.attribute("type")?.value?.toLowerCase()
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


        private fun ReadTradeitem(doc: EPCISBaseDocument, xTradeitem: XElement, type: String) {
            // read the GTIN from the id
            val id = xTradeitem.attribute("id")?.value ?: ""
            val tradeitem = Tradeitem()
            tradeitem.GTIN = Models.Identifiers.GTIN(id)
            tradeitem.EPCISType = type

            // read the object
            ReadMasterDataObject(tradeitem, xTradeitem)
            doc.MasterData.add(tradeitem)
        }


        private fun ReadLocation(doc: EPCISBaseDocument, xLocation: XElement, type: String) {
            // read the GLN from the id
            val id = xLocation.attribute("id")?.value ?: ""
            val t = Setup.MasterDataTypes[type]
            val location = if (Activator.CreateInstance(t) is Location) {
                throw Exception("Failed to create instance of Location from type $t")
            } else {
                loc.GLN = Models.Identifiers.GLN(id)
                loc.EPCISType = type

                // read the object
                ReadMasterDataObject(loc, xLocation)
                doc.MasterData.add(loc)
            }
        }


        private fun ReadTradingParty(doc: EPCISBaseDocument, xTradingParty: XElement, type: String) {
            // read the PGLN from the id
            val id = xTradingParty.attribute("id")?.value ?: ""
            val tp = TradingParty()
            tp.PGLN = Models.Identifiers.PGLN(id)
            tp.EPCISType = type

            // read the object
            ReadMasterDataObject(tp, xTradingParty)
            doc.MasterData.add(tp)
        }


        private fun ReadUnknown(doc: EPCISBaseDocument, xVocabElement: XElement, type: String) {
            // read the PGLN from the id
            val id = xVocabElement.attribute("id")?.value ?: ""
            val ele = VocabularyElement()
            ele.ID = id
            ele.EPCISType = type

            // read the object
            ReadMasterDataObject(ele, xVocabElement)
            doc.MasterData.add(ele)
        }



        private fun ReadMasterDataObject(md: IVocabularyElement, xMasterData: XElement, readKDEs: Boolean = true) {
            val mappedProperties = OTMappingTypeInformation.GetMasterDataXmlTypeInfo(md.javaClass)

            // work on expanded objects...
            // these are objects on the vocab element represented by one or more attributes in the EPCIS master data
            val ignoreAttributes = mutableListOf<String>()
            for (property in mappedProperties.Properties.filter { it.Name == "" }) {
                val subMappedProperties = OTMappingTypeInformation.GetMasterDataXmlTypeInfo(property.Property.type)
                var setAttribute = false
                val subObject = property.Property.type.createInstance()
                if (subObject != null) {
                    for (xeAtt in xMasterData.elements("attribute")) {
                        val id = xeAtt.attribute("id")?.value ?: ""
                        val propMapping = subMappedProperties[id]
                        if (propMapping != null) {
                            if (!TrySetValueType(xeAtt.value, propMapping.property, subObject)) {
                                val value = ReadKDEObject(xeAtt, propMapping.property.type)
                                propMapping.property.setValue(subObject, value)
                            }
                            setAttribute = true
                            ignoreAttributes.add(id)
                        }
                    }
                    if (setAttribute) {
                        property.Property.setValue(md, subObject)
                    }
                }
            }

            // go through each standard attribute...
            for (xeAtt in xMasterData.elements("attribute")) {
                val id = xeAtt.attribute("id")?.value ?: ""

                if (ignoreAttributes.contains(id)) {
                    continue
                }

                val propMapping = mappedProperties[id]
                if (propMapping != null) {
                    if (!TrySetValueType(xeAtt.value, propMapping.property, md)) {
                        val value = ReadKDEObject(xeAtt, propMapping.property.type)
                        propMapping.property.setValue(md, value)
                    }
                } else if (readKDEs) {
                    if (xeAtt.hasElements) {
                        // serialize into object kde...
                        val kdeObject = MasterDataKDEObject("", id)
                        kdeObject.SetFromEPCISXml(xeAtt)
                        md.KDEs.add(kdeObject)
                    } else {
                        // serialize into string kde
                        val kdeString = MasterDataKDEString("", id)
                        kdeString.SetFromEPCISXml(xeAtt)
                        md.KDEs.add(kdeString)
                    }
                }
            }
        }


        private fun ReadKDEObject(xeAtt: XElement, t: Type): Any {
            val value = t.createInstance() ?: throw Exception("Failed to create instance of ${t.fullName}")

            if (value is MutableList<*>) {
                val list = value as MutableList<Any>
                for (xchild in xeAtt.elements()) {
                    val child = ReadKDEObject(xchild, t.genericTypeArguments[0])
                    list.add(child)
                }
            } else {
                // go through each property...
                for (p in t.declaredProperties) {
                    val xmlAtt = p.getCustomAttribute<OpenTraceabilityAttribute>()
                    if (xmlAtt != null) {
                        val x = xeAtt.element(xmlAtt.name)
                        if (x != null) {
                            val objAtt = p.getCustomAttribute<OpenTraceabilityObjectAttribute>()
                            if (objAtt != null) {
                                val o = ReadKDEObject(x, p.type)
                            } else if (!TrySetValueType(x.value, p, value)) {
                                throw Exception("Failed to set value type while reading KDE object. property = ${p.name}, type = ${t.fullName}, xml = ${x.toString()}")
                            }
                        }
                    }
                }
            }

            return value
        }


        private fun TrySetValueType(`val`: String, p: PropertyInfo, o: Any): Boolean {
            when {
                p.propertyType == String::class.java -> {
                    p.setValue(o, `val`)
                    return true
                }
                p.propertyType == MutableList::class.java && p.genericType == List::class.java -> {
                    val cur = p.getValue(o) as MutableList<String>?
                    if (cur == null) {
                        val newList = mutableListOf<String>()
                        newList.add(`val`)
                        p.setValue(o, newList)
                    } else {
                        cur.add(`val`)
                    }
                    return true
                }
                p.propertyType == Boolean::class.java || p.propertyType == java.lang.Boolean::class.java -> {
                    val v = java.lang.Boolean.parseBoolean(`val`)
                    p.setValue(o, v)
                    return true
                }
                p.propertyType == Double::class.java || p.propertyType == java.lang.Double::class.java -> {
                    val v = java.lang.Double.parseDouble(`val`)
                    p.setValue(o, v)
                    return true
                }
                p.propertyType == URI::class.java -> {
                    val v = URI.create(`val`)
                    p.setValue(o, v)
                    return true
                }
                p.propertyType == MutableList::class.java && p.genericType == List::class.java -> {
                    val l = mutableListOf<LanguageString>()
                    l.add(LanguageString("en-US", `val`))
                    p.setValue(o, l)
                    return true
                }
                p.propertyType == Country::class.java -> {
                    val v = Countries.Parse(`val`)
                    p.setValue(o, v)
                    return true
                }
                p.propertyType == PGLN::class.java -> {
                    val v = PGLN(`val`)
                    p.setValue(o, v)
                    return true
                }
                else -> return false
            }
        }

    }
}
