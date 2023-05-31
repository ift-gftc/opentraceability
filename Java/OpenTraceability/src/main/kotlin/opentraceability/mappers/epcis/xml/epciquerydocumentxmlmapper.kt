package mappers.epcis.xml

import interfaces.*
import mappers.OpenTraceabilityXmlMapper
import models.events.*
import models.events.EPCISQueryDocument
import opentraceability.mappers.epcis.utils
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList

class EPCISQueryDocumentXMLMapper : IEPCISQueryDocumentMapper {
    override fun map(strValue: String, checkSchema: Boolean): EPCISQueryDocument {
        try {
            // TODO: validate the schema depending on the version in the document

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

            // read the query name
            val xQueryName = xDoc.documentElement?.element("EPCISBody")?.element(epcisQueryXName + "QueryResults")?.element("queryName")
            if (xQueryName != null) {
                document.QueryName = xQueryName.value
            }


// read the events
            val xEventList: Element? = xDoc.documentElement?.getChild("EPCISBody")?.getChild(epcisQueryXName)?.getChild("QueryResults")?.getChild("resultsBody")?.getChild("EventList")  ?: throw Exception("Failed to get EPCISBody/EventList after adding it to the XDoc.Root")

            if (xEventList != null) {
                for (xEvent in xEventList.children) {
                    var x: Element = xEvent
                    if (document.EPCISVersion == EPCISVersion.V1 && x.getChild("TransformationEvent") != null) {
                        x = xEvent.getChild("TransformationEvent")
                    }
                    val eventType = EPCISDocumentBaseXMLMapper.getEventTypeFromProfile(x)
                    val e = OpenTraceabilityXmlMapper.fromXml(x, eventType, document.EPCISVersion)
                    document.Events.add(e as IEvent)
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
            throw Exception("doc.EPCISVersion is NULL. This must be set to a version.")
        }

        val epcisNS = if (doc.EPCISVersion == EPCISVersion.V2) Constants.EPCISQUERY_2_NAMESPACE else Constants.EPCISQUERY_1_NAMESPACE

        val xDoc = EPCISDocumentBaseXMLMapper.WriteXml(doc, epcisNS, "EPCISQueryDocument")
        if (xDoc.root == null) {
            throw Exception("Failed to parse EPCISQueryDocument from xml string because after parsing the XDocument the Root property was null.")
        }

        val epcisQueryXName = if (doc.EPCISVersion == EPCISVersion.V1) Constants.EPCISQUERY_1_XNAMESPACE else Constants.EPCISQUERY_2_XNAMESPACE

        // write the query name
        xDoc.root.addElement(
            "EPCISBody",
            Element(epcisQueryXName + "QueryResults").apply {
                addElement("queryName")
                addElement("resultsBody", Element("EventList"))
            }
        )

        val xQueryName = xDoc.root?.element("EPCISBody")?.element(epcisQueryXName + "QueryResults")?.element("queryName")
        if (xQueryName != null) {
            xQueryName.value = doc.QueryName
        }

        // write the events
        val xEventList = xDoc.root?.element("EPCISBody")?.element(epcisQueryXName + "QueryResults")?.element("resultsBody")?.element("EventList")
            ?: throw Exception("Failed to get EPCISBody/EventList after adding it to the XDoc.Root")
        for (e in doc.Events) {
            val xname = EPCISDocumentBaseXMLMapper.GetEventXName(e)
            val xEvent = OpenTraceabilityXmlMapper.toXml(xname, e, doc.EPCISVersion)
            if (e.EventType == EventType.TransformationEvent && doc.EPCISVersion == EPCISVersion.V1) {
                xEvent?.let {
                    xEventList.addElement("extension", it)
                }
            } else {
                xEvent?.let {
                    xEventList.addElement(it)
                }
            }
        }

        EPCISDocumentBaseXMLMapper.ValidateEPCISQueryDocumentSchema(xDoc, doc.EPCISVersion)

        return xDoc.toString()
    }
}
