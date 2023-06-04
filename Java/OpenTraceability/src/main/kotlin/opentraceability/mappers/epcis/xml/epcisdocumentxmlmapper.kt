package opentraceability.mappers.epcis.xml

import opentraceability.interfaces.IEPCISDocumentMapper
import opentraceability.interfaces.IEvent
import opentraceability.mappers.OpenTraceabilityXmlMapper
import opentraceability.models.events.*
import opentraceability.utility.*
import org.w3c.dom.Element

class EPCISDocumentXMLMapper : IEPCISDocumentMapper {

    override fun map(strValue: String): EPCISDocument {
        try {
            //val xDoc: Document
            //val document: EPCISQueryDocument

            // TODO: validate the schema depending on the version in the document

            var (document, d)  = EPCISDocumentBaseXMLMapper.readXml<EPCISDocument>(strValue)
            var xDoc = d.documentElement

            if (document.epcisVersion == null) {
                throw Exception("doc.epcisVersion is NULL. This must be set to a version.")
            }

            EPCISDocumentBaseXMLMapper.validateEPCISQueryDocumentSchema(xDoc.ownerDocument, document.epcisVersion!!)

            val epcisQueryXName = if (document.epcisVersion == EPCISVersion.V1) {
                opentraceability.Constants.EPCISQUERY_1_XNAMESPACE
            } else {
                opentraceability.Constants.EPCISQUERY_2_XNAMESPACE
            }

            // read the events
            val xEventList = xDoc.element("EPCISBody")?.element(epcisQueryXName + "QueryResults")?.element("resultsBody")?.element("EventList")
                ?: throw Exception("Failed to get EPCISBody/EventList after adding it to the XDoc.Root")
            xEventList.childNodes.forEachIndex { xEvent, _ ->
                var x = xEvent
                if (xEvent != null && document.epcisVersion == EPCISVersion.V1 && x.element("TransformationEvent") != null)
                {
                    x = xEvent.element("TransformationEvent")!!
                }
                val eventType = EPCISDocumentBaseXMLMapper.getEventTypeFromProfile(x)
                val e = OpenTraceabilityXmlMapper.fromXml(x, eventType, document.epcisVersion!!)
                document.events.add(e as IEvent)
            }

            return document
        } catch (ex: Exception) {
            val exception = Exception("Failed to parse the EPCIS document from the XML. xml=$strValue", ex)
            opentraceability.OTLogger.error(exception)
            throw exception
        }
    }

    override fun map(doc: EPCISDocument): String {
        if (doc.epcisVersion == null) {
            throw Exception("doc.epcisVersion is NULL. This must be set to a version.")
        }

        val epcisNS = if (doc.epcisVersion == EPCISVersion.V2) {
            opentraceability.Constants.EPCISQUERY_2_NAMESPACE
        } else {
            opentraceability.Constants.EPCISQUERY_1_NAMESPACE
        }

        val xDoc = EPCISDocumentBaseXMLMapper.writeXml(doc, epcisNS, "EPCISQueryDocument").documentElement!!

        val epcisQueryXName = if (doc.epcisVersion == EPCISVersion.V1)
        {
            opentraceability.Constants.EPCISQUERY_1_XNAMESPACE
        }
        else
        {
            opentraceability.Constants.EPCISQUERY_2_XNAMESPACE
        }

        // write the query name
        var xQueryResults = xDoc.addElement("EPCISBody").addElementNS(epcisQueryXName, "QueryResults")
        xQueryResults.addElement("queryName")
        var xEventList = xQueryResults.addElement("resultsBody").addElement("EventList")

        for (e in doc.events)
        {
            val xname = EPCISDocumentBaseXMLMapper.getEventXName(e)
            var xEvent = OpenTraceabilityXmlMapper.toXml(xname, e, doc.epcisVersion!!) ?: throw Exception("Failed to deserialize event from XML.")
            if (e.eventType == EventType.TransformationEvent && doc.epcisVersion == EPCISVersion.V1) {
                var outerExtensionXml = createXmlElement("extension")
                outerExtensionXml.addElement(xEvent)
                xEvent = outerExtensionXml
            }
            if (xEvent != null)
            {
                xEventList.addElement(xEvent)
            }
        }

        EPCISDocumentBaseXMLMapper.validateEPCISQueryDocumentSchema(xDoc.ownerDocument, doc.epcisVersion!!)

        return xDoc.toString()
    }

}