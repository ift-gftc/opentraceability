package opentraceability.mappers.epcis.xml

import opentraceability.interfaces.IEvent
import opentraceability.mappers.OpenTraceabilityXmlMapper
import opentraceability.models.common.StandardBusinessDocumentHeader
import opentraceability.models.events.*
import opentraceability.utility.*
import java.lang.reflect.Type
import org.w3c.dom.*
import opentraceability.mappers.epcis.*
import opentraceability.utility.StringExtensions.parseXmlToDocument
import opentraceability.utility.StringExtensions.removeBOM
import opentraceability.utility.StringExtensions.tryConvertToDateTimeOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance

class EPCISDocumentBaseXMLMapper {
    companion object {

        var _schemaChecker: XmlSchemaChecker = XmlSchemaChecker()

        inline fun <reified T : EPCISBaseDocument> readXml(strValue: String): Pair<T, Document > {


            var xDoc = strValue.parseXmlToDocument()

            if (xDoc.documentElement == null) {
                throw Exception("Failed to parse EPCISBaseDocument from xml string because after parsing the XDocument the Root property was null.")
            }
            val document = T::class.createInstance()

            for (i in 0 until xDoc.documentElement.attributes.length) {
                val attribute = xDoc.documentElement.attributes.item(i) as org.w3c.dom.Attr

                if (attribute.name == "creationDate" || attribute.name == "schemaVersion") {
                    continue
                } else {
                    if (attribute?.namespaceURI == opentraceability.Constants.XMLNS_NAMEPSACE) {
                        document.namespaces[attribute.localName] = attribute.value
                    } else {
                        document.attributes[attribute.name.toString()] = attribute.value
                    }
                }
            }

            if (document.namespaces.values.contains(opentraceability.Constants.EPCIS_2_NAMESPACE) || document.namespaces.values.contains(
                    opentraceability.Constants.EPCISQUERY_2_NAMESPACE
                )
            ) {
                document.epcisVersion = EPCISVersion.V2
            } else if (document.namespaces.values.contains(opentraceability.Constants.EPCIS_1_NAMESPACE) || document.namespaces.values.contains(
                    opentraceability.Constants.EPCISQUERY_1_NAMESPACE
                )
            ) {
                document.epcisVersion = EPCISVersion.V1
            }

            if (document.epcisVersion == null) {
                throw Exception("Failed to determine the EPCIS version of the XML document. Must contain a namespace with either '${opentraceability.Constants.EPCIS_2_NAMESPACE}' or '${opentraceability.Constants.EPCIS_1_NAMESPACE}' or '${opentraceability.Constants.EPCISQUERY_2_NAMESPACE}' or '${opentraceability.Constants.EPCISQUERY_1_NAMESPACE}'")
            }

            val creationDateAttributeStr = xDoc.documentElement.getAttribute("creationDate")
            if (!creationDateAttributeStr.isNullOrBlank()) {
                document.creationDate = creationDateAttributeStr.tryConvertToDateTimeOffset()
            }

            val xHeader = xDoc.documentElement.getFirstElementByXPath("EPCISHeader" + opentraceability.Constants.SBDH_XNAMESPACE + "StandardBusinessDocumentHeader")
            if (xHeader != null) {
                document.header =
                    OpenTraceabilityXmlMapper.fromXml<StandardBusinessDocumentHeader>(xHeader, document.epcisVersion!!)
            }

            val xMasterData = xDoc.documentElement.getFirstElementByXPath("EPCISHeader/extension/EPCISMasterData")
            if (xMasterData != null) {
                EPCISXmlMasterDataReader.ReadMasterData(document, xMasterData)
            }
            return Pair(document, xDoc)
        }


        fun writeXml(doc: EPCISBaseDocument, epcisNS: String, rootEleName: String): Document {
            if (doc.epcisVersion == null) {
                throw Exception("doc.epcisVersion is NULL. This must be set to a version.")
            }

            var xDoc = createXmlElementNS(epcisNS, rootEleName)
            doc.attributes.map {
                xDoc.setAttribute(it.key, it.value)
            }

            for (ns in doc.namespaces) {
                if (ns.value == opentraceability.Constants.CBVMDA_NAMESPACE ||
                    ns.value == opentraceability.Constants.EPCISQUERY_1_NAMESPACE ||
                    ns.value == opentraceability.Constants.EPCISQUERY_2_NAMESPACE ||
                    ns.value == opentraceability.Constants.EPCIS_1_NAMESPACE ||
                    ns.value == opentraceability.Constants.EPCIS_2_NAMESPACE
                ) {
                    continue
                } else {
                    xDoc.setAttributeNS(opentraceability.Constants.XMLNS_XNAMESPACE, ns.key, ns.value)
                }
            }

            if (doc.creationDate != null)
            {
                xDoc.setAttribute("creationDate", doc.creationDate?.format(ISO_DATE_TIME))
            }

            if (doc.epcisVersion == EPCISVersion.V2) {
                xDoc.setAttribute("schemaVersion", "2.0")
                if (doc is EPCISQueryDocument) {
                    xDoc.setAttributeNS(opentraceability.Constants.XMLNS_XNAMESPACE, "epcisq", opentraceability.Constants.EPCISQUERY_2_NAMESPACE)
                    xDoc.setAttributeNS(opentraceability.Constants.XMLNS_XNAMESPACE, "cbvmda", opentraceability.Constants.CBVMDA_NAMESPACE)
                } else {
                    xDoc.setAttributeNS(opentraceability.Constants.XMLNS_XNAMESPACE, "epcis", opentraceability.Constants.EPCIS_2_NAMESPACE)
                    xDoc.setAttributeNS(opentraceability.Constants.XMLNS_XNAMESPACE, "cbvmda", opentraceability.Constants.CBVMDA_NAMESPACE)
                }
            } else if (doc.epcisVersion == EPCISVersion.V1) {
                xDoc.setAttribute("schemaVersion", "1.2")
                if (doc is EPCISQueryDocument) {
                    xDoc.setAttributeNS(opentraceability.Constants.XMLNS_XNAMESPACE, "epcisq", opentraceability.Constants.EPCISQUERY_1_NAMESPACE)
                    xDoc.setAttributeNS(opentraceability.Constants.XMLNS_XNAMESPACE, "cbvmda", opentraceability.Constants.CBVMDA_NAMESPACE)
                } else {
                    xDoc.setAttributeNS(opentraceability.Constants.XMLNS_XNAMESPACE, "epcis", opentraceability.Constants.EPCIS_1_NAMESPACE)
                    xDoc.setAttributeNS(opentraceability.Constants.XMLNS_XNAMESPACE, "cbvmda", opentraceability.Constants.CBVMDA_NAMESPACE)
                }
            }

            if (doc.header != null) {
                val xname = (opentraceability.Constants.SBDH_XNAMESPACE + "StandardBusinessDocumentHeader").toString()
                val xHeader = OpenTraceabilityXmlMapper.toXml(xname, doc.header, doc.epcisVersion!!)
                if (xHeader != null) {
                    var xEPCISHeader = createXmlElement("EPCISHeader")
                    xEPCISHeader.addElement(xHeader)
                    xDoc.addElement(xEPCISHeader)
                }
            }

            EPCISXmlMasterDataWriter.WriteMasterData(xDoc, doc)

            return xDoc.ownerDocument
        }


        internal fun getEventTypeFromProfile(xEvent: Element): KClass<IEvent> {
            val actionValue = xEvent.getFirstElementByXPath("action")?.nodeValue ?: ""
            val action = EventAction.valueOf(actionValue)
            val bizStep = xEvent.getFirstElementByXPath("bizStep")?.nodeValue
            var eventType = xEvent.nodeName

            if (eventType == "extension")
            {
                eventType = xEvent.firstChild.nodeName
            }

            val profiles = opentraceability.Setup.Profiles.filter {
                it.EventType.toString() == eventType &&
                        (it.Action == null || it.Action == action) &&
                        (it.BusinessStep == null || it.BusinessStep.lowercase() == bizStep?.lowercase())
            }.sortedByDescending { it.SpecificityScore }.toMutableList()

            if (profiles.isEmpty()) {
                throw Exception("Failed to create event from profile. Type=$eventType and BizStep=$bizStep and Action=$action")
            } else {
                profiles.filter { it.KDEProfiles != null }.forEach { profile ->
                    profile.KDEProfiles!!.forEach { kdeProfile ->
                        if (xEvent.getFirstElementByXPath(kdeProfile.XPath_V1) == null) {
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
            return when (e.eventType) {
                EventType.ObjectEvent -> "ObjectEvent"
                EventType.TransformationEvent -> "TransformationEvent"
                EventType.TransactionEvent -> "TransactionEvent"
                EventType.AggregationEvent -> "AggregationEvent"
                EventType.AssociationEvent -> "AssociationEvent"
                else -> throw Exception("Failed to determine the event xname. Event C# type is ${e.javaClass.name}")
            }
        }


        fun validateEPCISDocumentSchema(xdoc: Document, version: EPCISVersion) {
            val schemaUrl = if (version == EPCISVersion.V1) {
                "https://raw.githubusercontent.com/ift-gftc/doc.gdst/master/schemas/xml/epcis_1_2/EPCglobal-epcis-1_2.xsd"
            } else {
                "https://ref.gs1.org/standards/epcis/epcglobal-epcis-2_0.xsd"
            }

            var (isValid,error) = XmlSchemaChecker.validate(xdoc, schemaUrl)
            if (!isValid) {
                throw Exception("Failed to validate the XML schema for the EPCIS XML.\n$error")
            } else {
                throw Exception("Failed to validate the XML schema for the EPCIS XML.")
            }
        }


        fun validateEPCISQueryDocumentSchema(xdoc: Document, version: EPCISVersion) {
            val schemaUrl = if (version == EPCISVersion.V1) {
                "https://raw.githubusercontent.com/ift-gftc/doc.gdst/master/schemas/xml/epcis_1_2/EPCglobal-epcis-query-1_2.xsd"
            } else {
                "https://ref.gs1.org/standards/epcis/epcglobal-epcis-query-2_0.xsd"
            }

            var (isValid,error) = XmlSchemaChecker.validate(xdoc, schemaUrl)
            if (!isValid) {
                throw Exception("Failed to validate the XML schema for the EPCIS XML.\n$error")
            } else {
                throw Exception("Failed to validate the XML schema for the EPCIS XML.")
            }
        }
    }
}
