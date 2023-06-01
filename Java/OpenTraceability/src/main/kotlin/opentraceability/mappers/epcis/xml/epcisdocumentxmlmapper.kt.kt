package mappers.epcis.xml

import interfaces.IEPCISDocumentMapper
import interfaces.IEvent
import mappers.OpenTraceabilityXmlMapper
import models.events.*
import org.w3c.dom.Document
import org.w3c.dom.Element

class EPCISDocumentXMLMapper : IEPCISDocumentMapper {

    fun Map(strValue: String, checkSchema: Boolean = true): EPCISQueryDocument {
        try {
            //val xDoc: Document
            //val document: EPCISQueryDocument

            // TODO: validate the schema depending on the version in the document

            var (document, xDoc)  = EPCISDocumentBaseXMLMapper.readXml<EPCISQueryDocument>(strValue)

            if (xDoc.documentElement == null) {
                throw Exception("Failed to parse EPCISQueryDocument from xml string because after parsing the XDocument the Root property was null.")
            }
            if (document.EPCISVersion == null) {
                throw Exception("doc.EPCISVersion is NULL. This must be set to a version.")
            }

            if (checkSchema) {
                EPCISDocumentBaseXMLMapper.validateEPCISQueryDocumentSchema(xDoc, document.EPCISVersion!!)
            }

            val epcisQueryXName = if (document.EPCISVersion == EPCISVersion.V1) {
                Constants.EPCISQUERY_1_XNAMESPACE
            } else {
                Constants.EPCISQUERY_2_XNAMESPACE
            }

            // read the query name
            val xQueryName = xDoc.root?.element("EPCISBody")?.element(epcisQueryXName + "QueryResults")?.element("queryName")
            if (xQueryName != null) {
                document.QueryName = xQueryName.value
            }

            // read the events
            val xEventList = xDoc.root?.element("EPCISBody")?.element(epcisQueryXName + "QueryResults")?.element("resultsBody")?.element("EventList")
                ?: throw Exception("Failed to get EPCISBody/EventList after adding it to the XDoc.Root")
            xEventList.elements().forEach { xEvent ->
                var x = xEvent
                if (document.EPCISVersion == EPCISVersion.V1 && x.element("TransformationEvent") != null) {
                    x = xEvent.element("TransformationEvent")
                }
                val eventType = EPCISDocumentBaseXMLMapper.getEventTypeFromProfile(x)
                val e = OpenTraceabilityXmlMapper.fromXml(x, eventType, document.EPCISVersion!!)
                document.Events.add(e as IEvent)
            }

            return document
        } catch (ex: Exception) {
            val exception = Exception("Failed to parse the EPCIS document from the XML. xml=$strValue", ex)
            OTLogger.error(exception)
            throw exception
        }
    }



    fun Map(doc: EPCISQueryDocument): String {
        if (doc.EPCISVersion == null) {
            throw Exception("doc.EPCISVersion is NULL. This must be set to a version.")
        }

        val epcisNS = if (doc.EPCISVersion == EPCISVersion.V2) {
            Constants.EPCISQUERY_2_NAMESPACE
        } else {
            Constants.EPCISQUERY_1_NAMESPACE
        }

        val xDoc = EPCISDocumentBaseXMLMapper.writeXml(doc, epcisNS, "EPCISQueryDocument")
        if (xDoc.documentElement == null) {
            throw Exception("Failed to parse EPCISQueryDocument from xml string because after parsing the XDocument the Root property was null.")
        }

        val epcisQueryXName = if (doc.EPCISVersion == EPCISVersion.V1) {
            Constants.EPCISQUERY_1_XNAMESPACE
        } else {
            Constants.EPCISQUERY_2_XNAMESPACE
        }

        // write the query name
        xDoc.root!!.add(
            Element("EPCISBody",
                Element(epcisQueryXName + "QueryResults",
                    Element("queryName"),
                    Element("resultsBody",
                        Element("EventList"))
                )
            )
        )

        val xQueryName = xDoc.root?.element("EPCISBody")?.element(epcisQueryXName + "QueryResults")?.element("queryName")
        if (xQueryName != null) {
            xQueryName.Value = doc.QueryName
        }

        // write the events
        val xEventList = xDoc.root?.element("EPCISBody")?.element(epcisQueryXName + "QueryResults")?.element("resultsBody")?.element("EventList")
            ?: throw Exception("Failed to get EPCISBody/EventList after adding it to the XDoc.Root")
        for (e in doc.Events) {
            val xname = EPCISDocumentBaseXMLMapper.getEventXName(e)
            var xEvent = OpenTraceabilityXmlMapper.ToXml(xname, e, doc.EPCISVersion!!)
            if (e.EventType == EventType.TransformationEvent && doc.EPCISVersion == EPCISVersion.V1) {
                xEvent = Element("extension", xEvent)
            }
            if (xEvent != null) {
                xEventList.add(xEvent)
            }
        }

        EPCISDocumentBaseXMLMapper.validateEPCISQueryDocumentSchema(xDoc, doc.EPCISVersion!!)

        return xDoc.toString()
    }

}