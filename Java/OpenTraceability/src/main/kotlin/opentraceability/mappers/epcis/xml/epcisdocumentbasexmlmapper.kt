package mappers.epcis.xml

import interfaces.IEvent
import mappers.OpenTraceabilityXmlMapper
import models.common.StandardBusinessDocumentHeader
import models.events.*
import utility.*
import java.lang.reflect.Type
import org.w3c.dom.*
import opentraceability.mappers.epcis.*
import utility.StringExtensions.tryConvertToDateTimeOffset
import kotlin.reflect.full.createInstance

class EPCISDocumentBaseXMLMapper {
    companion object {

        var _schemaChecker: XmlSchemaChecker = XmlSchemaChecker()


        inline fun <reified T : EPCISBaseDocument> readXml(strValue: String): Pair<T, Document > {

            var xDoc = utils.parseXml(strValue)

            if (xDoc.documentElement == null) {
                throw Exception("Failed to parse EPCISBaseDocument from xml string because after parsing the XDocument the Root property was null.")
            }
            val document = T::class.createInstance()

            for (i in 0 until xDoc.documentElement.attributes.length) {
                val attribute = xDoc.documentElement.attributes.item(i) as org.w3c.dom.Attr

                if (attribute.name == "creationDate" || attribute.name == "schemaVersion") {
                    continue
                } else {
                    if (attribute?.namespaceURI == Constants.XMLNS_NAMEPSACE) {
                        document.Namespaces[attribute.localName] = attribute.value
                    } else {
                        document.Attributes[attribute.name.toString()] = attribute.value
                    }
                }
            }

            if (document.Namespaces.values.contains(Constants.EPCIS_2_NAMESPACE) || document.Namespaces.values.contains(
                    Constants.EPCISQUERY_2_NAMESPACE
                )
            ) {
                document.EPCISVersion = EPCISVersion.V2
            } else if (document.Namespaces.values.contains(Constants.EPCIS_1_NAMESPACE) || document.Namespaces.values.contains(
                    Constants.EPCISQUERY_1_NAMESPACE
                )
            ) {
                document.EPCISVersion = EPCISVersion.V1
            }
            if (document.EPCISVersion == null) {
                throw Exception("Failed to determine the EPCIS version of the XML document. Must contain a namespace with either '${Constants.EPCIS_2_NAMESPACE}' or '${Constants.EPCIS_1_NAMESPACE}' or '${Constants.EPCISQUERY_2_NAMESPACE}' or '${Constants.EPCISQUERY_1_NAMESPACE}'")
            }
            val creationDateAttributeStr = xDoc.documentElement.getAttribute("creationDate")
            if (!creationDateAttributeStr.isNullOrBlank()) {
                document.CreationDate = creationDateAttributeStr.tryConvertToDateTimeOffset()
            }
            val xHeader =
                xDoc.documentElement.getElementsByTagName("EPCISHeader")?.element(Constants.SBDH_XNAMESPACE + "StandardBusinessDocumentHeader")
            if (xHeader != null) {
                document.Header =
                    OpenTraceabilityXmlMapper.fromXml<StandardBusinessDocumentHeader>(xHeader, document.EPCISVersion!!)
            }


            val xMasterData = xDoc.documentElement.getElementsByTagName("EPCISHeader")?.element("extension")?.element("EPCISMasterData")
            if (xMasterData != null) {
                EPCISXmlMasterDataReader.ReadMasterData(document, xMasterData)
            }
            return Pair(document, xDoc)
        }


        fun writeXml(doc: EPCISBaseDocument, epcisNS: XNamespace, rootEleName: String): XDocument {
            if (doc.EPCISVersion == null) {
                throw Exception("doc.EPCISVersion is NULL. This must be set to a version.")
            }

            val xDoc = XDocument(
                Element(
                    epcisNS + rootEleName,
                    doc.Attributes.map { XAttribute(it.key, it.value) }
                )
            )
            if (xDoc.root == null) {
                throw Exception("Failed to convert EPCIS Document into XML because the XDoc.Root is NULL. This should not happen.")
            }

            for (ns in doc.Namespaces) {
                if (ns.value == Constants.CBVMDA_NAMESPACE ||
                    ns.value == Constants.EPCISQUERY_1_NAMESPACE ||
                    ns.value == Constants.EPCISQUERY_2_NAMESPACE ||
                    ns.value == Constants.EPCIS_1_NAMESPACE ||
                    ns.value == Constants.EPCIS_2_NAMESPACE
                ) {
                    continue
                } else {
                    xDoc.root.add(XAttribute(Constants.XMLNS_XNAMESPACE + ns.key, ns.value))
                }
            }

            if (doc.CreationDate != null) {
                xDoc.root.add(XAttribute("creationDate", doc.CreationDate.Value.toString("o")))
            }

            if (doc.EPCISVersion == EPCISVersion.V2) {
                xDoc.root.add(XAttribute("schemaVersion", "2.0"))
                if (doc is EPCISQueryDocument) {
                    xDoc.root.add(XAttribute(Constants.XMLNS_XNAMESPACE + "epcisq", Constants.EPCISQUERY_2_NAMESPACE))
                    xDoc.root.add(XAttribute(Constants.XMLNS_XNAMESPACE + "cbvmda", Constants.CBVMDA_NAMESPACE))
                } else {
                    xDoc.root.add(XAttribute(Constants.XMLNS_XNAMESPACE + "epcis", Constants.EPCIS_2_NAMESPACE))
                    xDoc.root.add(XAttribute(Constants.XMLNS_XNAMESPACE + "cbvmda", Constants.CBVMDA_NAMESPACE))
                }
            } else if (doc.EPCISVersion == EPCISVersion.V1) {
                xDoc.root.add(XAttribute("schemaVersion", "1.2"))
                if (doc is EPCISQueryDocument) {
                    xDoc.root.add(XAttribute(Constants.XMLNS_XNAMESPACE + "epcisq", Constants.EPCISQUERY_1_NAMESPACE))
                    xDoc.root.add(XAttribute(Constants.XMLNS_XNAMESPACE + "cbvmda", Constants.CBVMDA_NAMESPACE))
                } else {
                    xDoc.root.add(XAttribute(Constants.XMLNS_XNAMESPACE + "epcis", Constants.EPCIS_1_NAMESPACE))
                    xDoc.root.add(XAttribute(Constants.XMLNS_XNAMESPACE + "cbvmda", Constants.CBVMDA_NAMESPACE))
                }
            }

            if (doc.Header != null) {
                val xname = (Constants.SBDH_XNAMESPACE + "StandardBusinessDocumentHeader").toString()
                val xHeader = OpenTraceabilityXmlMapper.toXml(xname, doc.Header, doc.EPCISVersion!!)
                if (xHeader != null) {
                    xDoc.root.add(Element("EPCISHeader", xHeader))
                }
            }

            EPCISXmlMasterDataWriter.WriteMasterData(xDoc.root, doc)

            return xDoc
        }


        internal fun getEventTypeFromProfile(xEvent: Element): Type {
            val actionValue = xEvent.element("action")?.value
            val action = EventAction.valueOf(actionValue)
            val bizStep = xEvent.element("bizStep")?.value
            var eventType = xEvent.name.localName

            if (eventType == "extension") {
                eventType = xEvent.elements().first().name.localName
            }

            val profiles = Setup.Profiles.filter {
                it.EventType.toString() == eventType &&
                        (it.Action == null || it.Action == action) &&
                        (it.BusinessStep == null || it.BusinessStep.lowercase() == bizStep?.lowercase())
            }.sortedByDescending { it.specificityScore }

            if (profiles.isEmpty()) {
                throw Exception("Failed to create event from profile. Type=$eventType and BizStep=$bizStep and Action=$action")
            } else {
                profiles.filter { it.KDEProfiles != null }.forEach { profile ->
                    profile.KDEProfiles!!.forEach { kdeProfile ->
                        if (xEvent.queryXPath(kdeProfile.XPath_V1) == null) {
                            profiles.remove(profile)
                        }
                    }
                }

                if (profiles.isEmpty()) {
                    throw Exception("Failed to create event from profile. Type=$eventType and BizStep=$bizStep and Action=$action")
                }

                return profiles.first().EventClassType
            }
        }


        internal fun getEventXName(e: IEvent): String {
            return when (e.EventType) {
                EventType.ObjectEvent -> "ObjectEvent"
                EventType.TransformationEvent -> "TransformationEvent"
                EventType.TransactionEvent -> "TransactionEvent"
                EventType.AggregationEvent -> "AggregationEvent"
                EventType.AssociationEvent -> "AssociationEvent"
                else -> throw Exception("Failed to determine the event xname. Event C# type is ${e.javaClass.name}")
            }
        }


        fun validateEPCISDocumentSchema(xdoc: XDocument, version: EPCISVersion) {
            val schemaUrl = if (version == EPCISVersion.V1) {
                "https://raw.githubusercontent.com/ift-gftc/doc.gdst/master/schemas/xml/epcis_1_2/EPCglobal-epcis-1_2.xsd"
            } else {
                "https://ref.gs1.org/standards/epcis/epcglobal-epcis-2_0.xsd"
            }

            if (!EPCISDocumentBaseXMLMapper.SchemaChecker.validate(xdoc, schemaUrl, { error ->
                    throw OpenTraceabilitySchemaException("Failed to validate the XML schema for the EPCIS XML.\n$error")
                })) {
                throw OpenTraceabilitySchemaException("Failed to validate the XML schema for the EPCIS XML.")
            }
        }


        fun validateEPCISQueryDocumentSchema(xdoc: Document, version: EPCISVersion) {
            val schemaUrl = if (version == EPCISVersion.V1) {
                "https://raw.githubusercontent.com/ift-gftc/doc.gdst/master/schemas/xml/epcis_1_2/EPCglobal-epcis-query-1_2.xsd"
            } else {
                "https://ref.gs1.org/standards/epcis/epcglobal-epcis-query-2_0.xsd"
            }

            if (!EPCISDocumentBaseXMLMapper.SchemaChecker.validate(xdoc, schemaUrl, { error ->
                    throw Exception("Failed to validate the XML schema for the EPCIS XML.\n$error")
                })) {
                throw Exception("Failed to validate the XML schema for the EPCIS XML.")
            }
        }

    }
}
