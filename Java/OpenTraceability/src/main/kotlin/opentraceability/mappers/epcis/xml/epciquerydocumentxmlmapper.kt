package mappers.epcis.xml

import getFirstElementByXPath
import interfaces.IEvent
import interfaces.IEPCISQueryDocumentMapper
import mappers.OpenTraceabilityXmlMapper
import models.events.EPCISQueryDocument
import models.events.EPCISVersion
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
            if (document.EPCISVersion == null) {
                throw Exception("doc.EPCISVersion is NULL. This must be set to a version.")
            }

            if (checkSchema) {
                EPCISDocumentBaseXMLMapper.validateEPCISQueryDocumentSchema(xDoc, document.EPCISVersion!!)
            }

            val epcisQueryXName = if (document.EPCISVersion == EPCISVersion.V1) Constants.EPCISQUERY_1_XNAMESPACE else Constants.EPCISQUERY_2_XNAMESPACE

            val xQueryName = xDoc.documentElement?.getFirstElementByXPath("EPCISBody/$epcisQueryXName:QueryResults/queryName")
            if (xQueryName != null) {
                document.QueryName = xQueryName.nodeValue
            }

            val xEventList = xDoc.documentElement?.getFirstElementByXPath("EPCISBody/$epcisQueryXName:QueryResults/resultsBody/EventList")
                ?: throw Exception("Failed to get EPCISBody/EventList after adding it to the XDoc.Root")

            if (xEventList != null) {
                for (i in 0 until xEventList.childNodes.length) {
                    val xEvent = xEventList.childNodes.item(i) as? Element
                    if (xEvent != null) {
                        val eventType = EPCISDocumentBaseXMLMapper.getEventTypeFromProfile(xEvent)
                        val e = OpenTraceabilityXmlMapper.FromXml(xEvent, eventType, document.EPCISVersion!!)
                        document.Events.add(e as IEvent)
                    }
                }
            }

            return document
        } catch (ex: Exception) {
            val exception = Exception("Failed to parse the EPCIS document from the XML. xml=$strValue", ex)
            OTLogger.error(exception)
            throw exception
        }
    }

    override fun map(doc: EPCISQueryDocument): String {
        if (doc.EPCISVersion == null) {
            throw Exception("EPCISVersion is NULL. This must be set to a version.")
        }

        val epcisNS = if (doc.EPCISVersion == EPCISVersion.V2) Constants.EPCISQUERY_2_NAMESPACE else Constants.EPCISQUERY_1_NAMESPACE

        val xDoc = EPCISDocumentBaseXMLMapper.writeXml(doc, epcisNS, "EPCISQueryDocument")
        if (xDoc.documentElement == null) {
            throw Exception("Failed to parse EPCISQueryDocument from xml string because after parsing the XDocument the Root property was null.")
        }

        val epcisQueryXName = if (doc.EPCISVersion == models.events.EPCISVersion.V1) Constants.EPCISQUERY_1_XNAMESPACE else Constants.EPCISQUERY_2_XNAMESPACE

        // write the query name
        val xQueryResults = xDoc.createElementNS(epcisQueryXName, "QueryResults")
        xQueryResults.appendChild(xDoc.createElement("queryName"))
        val resultsBody = xDoc.createElement("resultsBody")
        resultsBody.appendChild(xDoc.createElement("EventList"))
        xQueryResults.appendChild(resultsBody)
        xDoc.documentElement?.getElementsByTagName("EPCISBody")?.item(0)?.appendChild(xQueryResults)

        val xQueryName = xDoc.documentElement?.getFirstElementByXPath("EPCISBody/$epcisQueryXName:QueryResults/queryName");
        if (xQueryName != null) {
            xQueryName.nodeValue = doc.QueryName
        }

        // write the events
        val xEventList = xDoc.documentElement?.getFirstElementByXPath("EPCISBody/$epcisQueryXName:QueryResults/resultsBody/EventList")
            ?: throw Exception("Failed to get EPCISBody/EventList after adding it to the XDoc.Root")
        for (e in doc.Events) {
            val xname = EPCISDocumentBaseXMLMapper.getEventXName(e)
            val xEvent = OpenTraceabilityXmlMapper.ToXml(xname, e, doc.EPCISVersion!!)
            if (e.EventType == models.events.EventType.TransformationEvent && doc.EPCISVersion == EPCISVersion.V1) {
                val extension = xDoc.createElement("extension")
                extension.appendChild(xEvent)
                xEventList.appendChild(extension)
            } else {
                xEvent?.let { xEventList.appendChild(it) }
            }
        }

        EPCISDocumentBaseXMLMapper.validateEPCISQueryDocumentSchema(xDoc, doc.EPCISVersion!!)

        return docToString(xDoc)
    }

    fun docToString(doc: org.w3c.dom.Document): String {
        val transformerFactory = TransformerFactory.newInstance()
        val transformer = transformerFactory.newTransformer()
        val writer = StringWriter()
        transformer.transform(DOMSource(doc), StreamResult(writer))
    }
}
