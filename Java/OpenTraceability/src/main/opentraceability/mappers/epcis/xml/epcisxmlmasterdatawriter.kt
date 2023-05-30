package mappers.epcis.xml

import interfaces.IVocabularyElement
import mappers.OTMappingTypeInformation
import models.common.LanguageString
import javax.xml.bind.annotation.*
import models.identifiers.*
import models.events.*
import models.events.EPCISBaseDocument
import utility.attributes.OpenTraceabilityArrayAttribute
import utility.attributes.OpenTraceabilityAttribute
import utility.attributes.OpenTraceabilityObjectAttribute

class EPCISXmlMasterDataWriter {
    companion object {

        fun WriteMasterData(xDocument: XElement, doc: EPCISBaseDocument) {
            if (doc.MasterData.size > 0) {
                var xEPCISHeader = xDocument.element("EPCISHeader")
                if (xEPCISHeader == null) {
                    xDocument.add(XElement("EPCISHeader", XElement("extension", XElement("EPCISMasterData", XElement("VocabularyList")))))
                } else {
                    xEPCISHeader.add(XElement("extension", XElement("EPCISMasterData", XElement("VocabularyList"))))
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

        private fun WriteMasterDataList(data: MutableList<IVocabularyElement>, xVocabList: XElement, type: String) {
            if (data.isNotEmpty()) {
                val xVocab = XElement("Vocabulary", XAttribute("type", type), XElement("VocabularyElementList"))
                val xVocabEleList = xVocab.element("VocabularyElementList") ?: throw Exception("Failed to grab the element VocabularyElementList")

                for (md in data) {
                    val xMD = WriteMasterDataObject(md)
                    xVocabEleList.add(xMD)
                }

                xVocabList.add(xVocab)
            }
        }

        private fun WriteMasterDataObject(md: IVocabularyElement): XElement {
            val xVocabEle = XElement("VocabularyElement")
            xVocabEle.add(XAttribute("id", md.ID ?: ""))

            val mappings = OTMappingTypeInformation.GetMasterDataXmlTypeInfo(md::class.java)

            for (mapping in mappings.Properties) {
                val id = mapping.Name
                val p = mapping.Property

                val o = p.get(md)


                if (o != null) {
                    if (id.isEmpty()) {
                        val subMappings = OTMappingTypeInformation.GetMasterDataXmlTypeInfo(o::class.java)
                        for (subMapping in subMappings.Properties) {
                            val subID = subMapping.Name
                            val subProperty = subMapping.Property
                            val subObj = subProperty.get(o)
                            if (subObj != null) {
                                if (subObj is MutableList<LanguageString>) {
                                    val l = subObj
                                    val str = l.firstOrNull()?.Value
                                    if (str != null) {
                                        val xAtt = XElement("attribute", XAttribute("id", subID))
                                        xAtt.value = str
                                        xVocabEle.add(xAtt)
                                    }
                                } else {
                                    val str = subObj.toString()
                                    if (str.isNotEmpty()) {
                                        val xAtt = XElement("attribute", XAttribute("id", subID))
                                        xAtt.value = str
                                        xVocabEle.add(xAtt)
                                    }
                                }
                            }
                        }
                    } else if (p.getAnnotation(OpenTraceabilityObjectAttribute::class.java) != null) {
                        val xAtt = XElement("attribute", XAttribute("id", id))
                        WriteObject(xAtt, p.type, o)
                        xVocabEle.add(xAtt)
                    } else if (p.getAnnotation(OpenTraceabilityArrayAttribute::class.java) != null) {
                        val l = o as List<*>
                        for (i in l) {
                            val str = i?.toString()
                            if (!str.isNullOrEmpty()) {
                                val xAtt = XElement("attribute", XAttribute("id", id))
                                xAtt.value = str
                                xVocabEle.add(xAtt)
                            }
                        }
                    } else if (o is MutableList<LanguageString>) {
                        val l = o
                        val str = l.firstOrNull()?.Value
                        if (str != null) {
                            val xAtt = XElement("attribute", XAttribute("id", id))
                            xAtt.value = str
                            xVocabEle.add(xAtt)
                        }
                    } else {
                        val str = o.toString()
                        if (str.isNotEmpty()) {
                            val xAtt = XElement("attribute", XAttribute("id", id))
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

        private fun WriteObject(x: XElement, t: Class<*>, o: Any) {
            for (property in t.declaredFields) {
                val value = property.get(o)
                if (value != null) {
                    val xmlAtt = property.getAnnotation(OpenTraceabilityAttribute::class.java)
                    if (xmlAtt != null) {
                        val xChild = XElement(xmlAtt.Name)
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
