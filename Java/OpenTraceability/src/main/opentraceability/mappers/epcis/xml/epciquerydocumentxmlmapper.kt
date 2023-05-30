package mappers.epcis.xml

import interfaces.IEPCISQueryDocumentMapper
import mappers.OpenTraceabilityXmlMapper
import models.events.*
import models.events.EPCISQueryDocument

class EPCISQueryDocumentXMLMapper : IEPCISQueryDocumentMapper {
    override fun map(strValue: String, checkSchema: Boolean): EPCISQueryDocument {
        try {
            // TODO: validate the schema depending on the version in the document

            val xDoc = XDocument.Parse(strValue)
            val document = EPCISDocumentBaseXMLMapper.ReadXml<EPCISQueryDocument>(strValue, xDoc)
            if (xDoc.root == null) {
                throw Exception("Failed to parse EPCISQueryDocument from xml string because after parsing the XDocument the Root property was null.")
            }
            if (document.EpcisVersion == null) {
                throw Exception("doc.EPCISVersion is NULL. This must be set to a version.")
            }

            if (checkSchema) {
                EPCISDocumentBaseXMLMapper.ValidateEPCISQueryDocumentSchema(xDoc, document.epcisVersion)
            }

            val epcisQueryXName = if (document.EpcisVersion == EPCISVersion.V1) Constants.EPCISQUERY_1_XNAMESPACE else Constants.EPCISQUERY_2_XNAMESPACE

            // read the query name
            val xQueryName = xDoc.root?.element("EPCISBody")?.element(epcisQueryXName + "QueryResults")?.element("queryName")
            if (xQueryName != null) {
                document.QueryName = xQueryName.value
            }

            // read the events
            val xEventList = xDoc.root?.element("EPCISBody")?.element(epcisQueryXName + "QueryResults")?.element("resultsBody")?.element("EventList")
                ?: throw Exception("Failed to get EPCISBody/EventList after adding it to the XDoc.Root")
            for (xEvent in xEventList.elements()) {
                var x = xEvent
                if (document.EpcisVersion == EPCISVersion.V1 && x.element("TransformationEvent") != null) {
                    x = xEvent.element("TransformationEvent")
                }
                val eventType = EPCISDocumentBaseXMLMapper.getEventTypeFromProfile(x)
                val e = OpenTraceabilityXmlMapper.fromXml(x, eventType, document.epcisVersion)
                document.Events.add(e as IEvent)
            }

            return document
        } catch (ex: Exception) {
            val exception = Exception("Failed to parse the EPCIS document from the XML. xml=$strValue", ex)
            OTLogger.error(exception)
            throw exception
        }
    }

    override fun map(doc: EPCISQueryDocument): String {
        if (doc.epcisVersion == null) {
            throw Exception("doc.EPCISVersion is NULL. This must be set to a version.")
        }

        val epcisNS = if (doc.epcisVersion == EPCISVersion.V2) Constants.EPCISQUERY_2_NAMESPACE else Constants.EPCISQUERY_1_NAMESPACE

        val xDoc = EPCISDocumentBaseXMLMapper.WriteXml(doc, epcisNS, "EPCISQueryDocument")
        if (xDoc.root == null) {
            throw Exception("Failed to parse EPCISQueryDocument from xml string because after parsing the XDocument the Root property was null.")
        }

        val epcisQueryXName = if (doc.epcisVersion == EPCISVersion.V1) Constants.EPCISQUERY_1_XNAMESPACE else Constants.EPCISQUERY_2_XNAMESPACE

        // write the query name
        xDoc.root.addElement(
            "EPCISBody",
            XElement(epcisQueryXName + "QueryResults").apply {
                addElement("queryName")
                addElement("resultsBody", XElement("EventList"))
            }
        )

        val xQueryName = xDoc.root?.element("EPCISBody")?.element(epcisQueryXName + "QueryResults")?.element("queryName")
        if (xQueryName != null) {
            xQueryName.value = doc.QueryName
        }

        // write the events
        val xEventList = xDoc.root?.element("EPCISBody")?.element(epcisQueryXName + "QueryResults")?.element("resultsBody")?.element("EventList")
            ?: throw Exception("Failed to get EPCISBody/EventList after adding it to the XDoc.Root")
        for (e in doc.events) {
            val xname = EPCISDocumentBaseXMLMapper.GetEventXName(e)
            val xEvent = OpenTraceabilityXmlMapper.toXml(xname, e, doc.epcisVersion)
            if (e.eventType == EventType.TransformationEvent && doc.epcisVersion == EPCISVersion.V1) {
                xEvent?.let {
                    xEventList.addElement("extension", it)
                }
            } else {
                xEvent?.let {
                    xEventList.addElement(it)
                }
            }
        }

        EPCISDocumentBaseXMLMapper.ValidateEPCISQueryDocumentSchema(xDoc, doc.epcisVersion)

        return xDoc.toString()
    }
}
