package opentraceability.mappers.epcis.xml

import opentraceability.utility.*
import opentraceability.interfaces.IEvent
import opentraceability.interfaces.IEPCISQueryDocumentMapper
import opentraceability.mappers.OpenTraceabilityXmlMapper
import opentraceability.models.events.EPCISQueryDocument
import opentraceability.models.events.EPCISVersion
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class EPCISQueryDocumentXMLMapper : IEPCISQueryDocumentMapper {
    override fun map(strValue: String, checkSchema: Boolean): EPCISQueryDocument {
        try {
            var (document, xDoc) = EPCISDocumentBaseXMLMapper.readXml<EPCISQueryDocument>(strValue)

            if (xDoc.documentElement == null) {
                throw Exception("Failed to parse EPCISQueryDocument from xml string because after parsing the XDocument the Root property was null.")
            }
            if (document.epcisVersion == null) {
                throw Exception("doc.epcisVersion is NULL. This must be set to a version.")
            }

            if (checkSchema) {
                EPCISDocumentBaseXMLMapper.validateEPCISQueryDocumentSchema(xDoc, document.epcisVersion!!)
            }

            val epcisQueryXName =
                if (document.epcisVersion == EPCISVersion.V1) opentraceability.Constants.EPCISQUERY_1_XNAMESPACE else opentraceability.Constants.EPCISQUERY_2_XNAMESPACE

            val xQueryName =
                xDoc.documentElement?.getFirstElementByXPath("EPCISBody/$epcisQueryXName:QueryResults/queryName")
            if (xQueryName != null) {
                document.QueryName = xQueryName.nodeValue
            }

            val xEventList =
                xDoc.documentElement?.getFirstElementByXPath("EPCISBody/$epcisQueryXName:QueryResults/resultsBody/EventList")
                    ?: throw Exception("Failed to get EPCISBody/EventList after adding it to the XDoc.Root")

            if (xEventList != null) {
                for (i in 0 until xEventList.childNodes.length) {
                    val xEvent = xEventList.childNodes.item(i) as? Element
                    if (xEvent != null) {
                        val eventType = EPCISDocumentBaseXMLMapper.getEventTypeFromProfile(xEvent)
                        val e = OpenTraceabilityXmlMapper.fromXml(xEvent, eventType, document.epcisVersion!!)
                        document.events.add(e as IEvent)
                    }
                }
            }

            return document
        } catch (ex: Exception) {
            val exception = Exception("Failed to parse the EPCIS document from the XML. xml=$strValue", ex)
            opentraceability.OTLogger.error(exception)
            throw exception
        }
    }

    override fun map(doc: EPCISQueryDocument): String {
        if (doc.epcisVersion == null) {
            throw Exception("EPCISVersion is NULL. This must be set to a version.")
        }

        val epcisNS =
            if (doc.epcisVersion == EPCISVersion.V2) opentraceability.Constants.EPCISQUERY_2_NAMESPACE else opentraceability.Constants.EPCISQUERY_1_NAMESPACE

        val xDoc = EPCISDocumentBaseXMLMapper.writeXml(doc, epcisNS, "EPCISQueryDocument")
        if (xDoc.documentElement == null) {
            throw Exception("Failed to parse EPCISQueryDocument from xml string because after parsing the XDocument the Root property was null.")
        }

        val epcisQueryXName =
            if (doc.epcisVersion == opentraceability.models.events.EPCISVersion.V1) opentraceability.Constants.EPCISQUERY_1_XNAMESPACE else opentraceability.Constants.EPCISQUERY_2_XNAMESPACE

        // write the query name
        val xQueryResults = xDoc.createElementNS(epcisQueryXName, "QueryResults")
        xQueryResults.appendChild(xDoc.createElement("queryName"))
        val resultsBody = xDoc.createElement("resultsBody")
        resultsBody.appendChild(xDoc.createElement("EventList"))
        xQueryResults.appendChild(resultsBody)
        xDoc.documentElement?.getElementsByTagName("EPCISBody")?.item(0)?.appendChild(xQueryResults)

        val xQueryName =
            xDoc.documentElement?.getFirstElementByXPath("EPCISBody/$epcisQueryXName:QueryResults/queryName");
        if (xQueryName != null) {
            xQueryName.nodeValue = doc.QueryName
        }

        // write the events
        val xEventList =
            xDoc.documentElement?.getFirstElementByXPath("EPCISBody/$epcisQueryXName:QueryResults/resultsBody/EventList")
                ?: throw Exception("Failed to get EPCISBody/EventList after adding it to the XDoc.Root")
        for (e in doc.events) {
            val xname = EPCISDocumentBaseXMLMapper.getEventXName(e)
            val xEvent = OpenTraceabilityXmlMapper.toXml(xname, e, doc.epcisVersion!!)
            if (e.eventType == opentraceability.models.events.EventType.TransformationEvent && doc.epcisVersion == EPCISVersion.V1) {
                val extension = xDoc.createElement("extension")
                extension.appendChild(xEvent)
                xEventList.appendChild(extension)
            } else {
                xEvent?.let { xEventList.appendChild(it) }
            }
        }

        EPCISDocumentBaseXMLMapper.validateEPCISQueryDocumentSchema(xDoc, doc.epcisVersion!!)

        return docToString(xDoc)
    }

    fun docToString(doc: org.w3c.dom.Document): String {
        val transformerFactory = TransformerFactory.newInstance()
        val transformer = transformerFactory.newTransformer()
        val writer = StringWriter()
        transformer.transform(DOMSource(doc), StreamResult(writer))
        return writer.toString()
    }
}
