package mappers.epcis.xml

import interfaces.IVocabularyElement
import mappers.OTMappingTypeInformation
import models.common.LanguageString
import models.identifiers.*
import models.events.*
import models.events.EPCISBaseDocument
import utility.attributes.*
import org.jdom2.Element

class EPCISXmlMasterDataWriter {
    companion object {

        fun WriteMasterData(xDocument: Element, doc: EPCISBaseDocument) {
            if (doc.MasterData.size > 0) {
                var xEPCISHeader = xDocument.element("EPCISHeader")
                if (xEPCISHeader == null) {
                    xDocument.add(Element("EPCISHeader", Element("extension", Element("EPCISMasterData", Element("VocabularyList")))))
                } else {
                    xEPCISHeader.add(Element("extension", Element("EPCISMasterData", Element("VocabularyList"))))
                }

                val xVocabList = xDocument.xpathSelectElement("EPCISHeader/extension/EPCISMasterData/VocabularyList")
                    ?: throw Exception("Failed to grab the element EPCISHeader/extension/EPCISMasterData/VocabularyList.")

                for (mdList in doc.MasterData.groupBy { it.EPCISType }) {
                    if (mdList.key != null) {
                        WriteMasterDataList(mdList.value, xVocabList, mdList.key)
                    } else {
                        throw Exception("There are master data vocabulary elements where the Type is NULL.")
                    }
                }
            }
        }

        fun WriteMasterDataList(data: MutableList<IVocabularyElement>, xVocabList: Element, type: String) {
            if (data.isNotEmpty()) {
                val xVocab = Element("Vocabulary", XAttribute("type", type), Element("VocabularyElementList"))
                val xVocabEleList = xVocab.element("VocabularyElementList") ?: throw Exception("Failed to grab the element VocabularyElementList")

                for (md in data) {
                    val xMD = WriteMasterDataObject(md)
                    xVocabEleList.add(xMD)
                }

                xVocabList.add(xVocab)
            }
        }

        fun WriteMasterDataObject(md: IVocabularyElement): Element {
            val xVocabEle = Element("VocabularyElement")
            xVocabEle.add(XAttribute("id", md.ID ?: ""))

            val mappings = OTMappingTypeInformation.getMasterDataXmlTypeInfo(md::class.java)

            for (mapping in mappings.Properties) {
                val id = mapping.Name
                val p = mapping.Property

                val o = p.get(md)


                if (o != null) {
                    if (id.isEmpty()) {
                        val subMappings = OTMappingTypeInformation.getMasterDataXmlTypeInfo(o::class.java)
                        for (subMapping in subMappings.Properties) {
                            val subID = subMapping.Name
                            val subProperty = subMapping.Property
                            val subObj = subProperty.get(o)
                            if (subObj != null) {
                                if (subObj is MutableList<LanguageString>) {
                                    val l = subObj
                                    val str = l.firstOrNull()?.Value
                                    if (str != null) {
                                        val xAtt = Element("attribute", XAttribute("id", subID))
                                        xAtt.value = str
                                        xVocabEle.add(xAtt)
                                    }
                                } else {
                                    val str = subObj.toString()
                                    if (str.isNotEmpty()) {
                                        val xAtt = Element("attribute", XAttribute("id", subID))
                                        xAtt.value = str
                                        xVocabEle.add(xAtt)
                                    }
                                }
                            }
                        }
                    } else if (p.getAnnotation(OpenTraceabilityObjectAttribute::class.java) != null) {
                        val xAtt = Element("attribute", XAttribute("id", id))
                        WriteObject(xAtt, p.type, o)
                        xVocabEle.add(xAtt)
                    } else if (p.getAnnotation(OpenTraceabilityArrayAttribute::class.java) != null) {
                        val l = o as List<*>
                        for (i in l) {
                            val str = i?.toString()
                            if (!str.isNullOrEmpty()) {
                                val xAtt = Element("attribute", XAttribute("id", id))
                                xAtt.value = str
                                xVocabEle.add(xAtt)
                            }
                        }
                    } else if (o is MutableList<LanguageString>) {
                        val l = o
                        val str = l.firstOrNull()?.Value
                        if (str != null) {
                            val xAtt = Element("attribute", XAttribute("id", id))
                            xAtt.value = str
                            xVocabEle.add(xAtt)
                        }
                    } else {
                        val str = o.toString()
                        if (str.isNotEmpty()) {
                            val xAtt = Element("attribute", XAttribute("id", id))
                            xAtt.value = str
                            xVocabEle.add(xAtt)
                        }
                    }
                }

            }

            for (kde in md.KDEs) {
                val xKDE = kde.GetEPCISXml()
                if (xKDE != null) {
                    xVocabEle.add(xKDE)
                }
            }

            return xVocabEle
        }

        fun WriteObject(x: Element, t: Class<*>, o: Any) {
            for (property in t.declaredFields) {
                val value = property.get(o)
                if (value != null) {
                    val xmlAtt = property.getAnnotation(OpenTraceabilityAttribute::class.java)
                    if (xmlAtt != null) {
                        val xChild = Element(xmlAtt.Name)
                        if (property.getAnnotation(OpenTraceabilityObjectAttribute::class.java) != null) {
                            WriteObject(xChild, property.type, value)
                        } else {
                            val str = value.toString()
                            if (str != null) {
                                xChild.value = str
                            }
                        }
                        x.add(xChild)
                    }
                }
            }
        }


    }
}
