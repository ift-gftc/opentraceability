package opentraceability.mappers.epcis.xml

import opentraceability.interfaces.IVocabularyElement
import opentraceability.mappers.OTMappingTypeInformation
import opentraceability.models.common.LanguageString
import opentraceability.models.events.EPCISBaseDocument
import opentraceability.utility.*
import org.w3c.dom.Element
import opentraceability.utility.attributes.*
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.typeOf

class EPCISXmlMasterDataWriter {
    companion object {

        fun WriteMasterData(xDocument: Element, doc: EPCISBaseDocument) {
            if (doc.masterData.size > 0) {
                var xEPCISHeader = xDocument.element("EPCISHeader")
                if (xEPCISHeader == null) {
                    xDocument.addElement("EPCISHeader").addElement("extension").addElement("EPCISMasterData").addElement("VocabularyList")
                } else {
                    xEPCISHeader.addElement("extension").addElement("EPCISMasterData").addElement("VocabularyList")
                }

                val xVocabList = xDocument.getFirstElementByXPath("EPCISHeader/extension/EPCISMasterData/VocabularyList")
                    ?: throw Exception("Failed to grab the element EPCISHeader/extension/EPCISMasterData/VocabularyList.")

                for (mdList in doc.masterData.groupBy { it.epcisType }) {
                    if (mdList.key != null) {
                        WriteMasterDataList(mdList.value.toMutableList(), xVocabList, mdList.key!!)
                    } else {
                        throw Exception("There are master data vocabulary elements where the Type is NULL.")
                    }
                }
            }
        }

        fun WriteMasterDataList(data: MutableList<IVocabularyElement>, xVocabList: Element, type: String) {
            if (data.isNotEmpty()) {
                val xVocab = createXmlElement(("Vocabulary"))
                xVocab.setAttribute("type", type)
                val xVocabEleList = xVocab.addElement("VocabularyElementList")

                for (md in data) {
                    val xMD = WriteMasterDataObject(md)
                    xVocabEleList.addElement(xMD)
                }

                xVocabList.addElement(xVocab)
            }
        }

        fun WriteMasterDataObject(md: IVocabularyElement): Element {
            val xVocabEle = createXmlElement("VocabularyElement")
            xVocabEle.setAttribute("id", md.id)

            val mappings = OTMappingTypeInformation.getMasterDataXmlTypeInfo(md::class.starProjectedType as KClass<*>)

            for (mapping in mappings.properties) {
                val id = mapping.Name
                val p = mapping.Property

                val o = p.getter.call(md)

                if (o != null) {
                    if (id.isEmpty()) {
                        val subMappings = OTMappingTypeInformation.getMasterDataXmlTypeInfo(o::class.starProjectedType as KClass<*>)
                        for (subMapping in subMappings.properties) {
                            val subID = subMapping.Name
                            val subProperty = subMapping.Property
                            val subObj = subProperty.getter.call(o)
                            if (subObj != null) {
                                if (subObj::class.starProjectedType == typeOf<MutableList<LanguageString>>()) {
                                    val l = subObj as MutableList<LanguageString>
                                    val str = l.firstOrNull()?.value
                                    if (str != null) {
                                        val xAtt = xVocabEle.addElement("attribute")
                                        xAtt.nodeValue = str
                                        xAtt.setAttribute("id", subID)
                                    }
                                } else {
                                    val str = subObj.toString()
                                    if (str.isNotEmpty()) {
                                        val xAtt = xVocabEle.addElement("attribute")
                                        xAtt.nodeValue = str
                                        xAtt.setAttribute("id", subID)
                                    }
                                }
                            }
                        }
                    } else if (p.annotations.filterIsInstance<OpenTraceabilityObjectAttribute>().isNotEmpty()) {
                        val xAtt = xVocabEle.addElement("attribute")
                        xAtt.setAttribute("id", id)
                        WriteObject(xAtt, p.returnType as KClass<*>, o)
                    } else if (p.annotations.filterIsInstance<OpenTraceabilityArrayAttribute>().isNotEmpty()) {
                        val l = o as MutableList<*>
                        for (i in l) {
                            val str = i?.toString()
                            if (!str.isNullOrEmpty()) {
                                val xAtt = xVocabEle.addElement("attribute")
                                xAtt.setAttribute("id", id)
                                xAtt.nodeValue = str
                            }
                        }
                    } else if (o::class.starProjectedType == typeOf<MutableList<LanguageString>>()) {
                        val l = o as MutableList<LanguageString>
                        val str = l.firstOrNull()?.value
                        if (str != null) {
                            val xAtt = xVocabEle.addElement("attribute")
                            xAtt.setAttribute("id", id)
                            xAtt.nodeValue = str
                        }
                    } else {
                        val str = o.toString()
                        if (str.isNotEmpty()) {
                            val xAtt = xVocabEle.addElement("attribute")
                            xAtt.setAttribute("id", id)
                            xAtt.nodeValue = str
                        }
                    }
                }

            }

            for (kde in md.kdes) {
                val xKDE = kde.getEPCISXml()
                if (xKDE != null) {
                    xVocabEle.addElement(xKDE)
                }
            }

            return xVocabEle
        }

        fun WriteObject(x: Element, t: KClass<*>, o: Any) {
            for (property in t.memberProperties) {
                val value = property.getter.call(o)
                if (value != null) {
                    val xmlAtt = property.annotations.filterIsInstance<OpenTraceabilityAttribute>().firstOrNull()
                    if (xmlAtt != null) {
                        val xChild = x.addElementNS(xmlAtt.ns, xmlAtt.name)
                        if (property.annotations.filterIsInstance<OpenTraceabilityObjectAttribute>().isNotEmpty())
                        {
                            WriteObject(xChild, property.returnType as KClass<*>, value)
                        } else {
                            val str = value.toString()
                            if (str != null) {
                                xChild.nodeValue = str
                            }
                        }
                    }
                }
            }
        }
    }
}
