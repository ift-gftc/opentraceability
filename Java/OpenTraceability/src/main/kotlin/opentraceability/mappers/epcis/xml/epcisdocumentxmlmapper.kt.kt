package mappers.epcis.xml

import interfaces.IEPCISDocumentMapper
import interfaces.IEvent
import mappers.OpenTraceabilityXmlMapper
import models.events.*
import models.events.EPCISDocument

class EPCISDocumentXMLMapper : IEPCISDocumentMapper {

    fun Map(strValue: String, checkSchema: Boolean = true): EPCISQueryDocument {
        try {
            val xDoc: XDocument
            val document: EPCISQueryDocument

            // TODO: validate the schema depending on the version in the document

            document = EPCISDocumentBaseXMLMapper.ReadXml<EPCISQueryDocument>(strValue, xDoc)
            if (xDoc.root == null) {
                throw Exception("Failed to parse EPCISQueryDocument from xml string because after parsing the XDocument the Root property was null.")
            }
            if (document.EPCISVersion == null) {
                throw Exception("doc.EPCISVersion is NULL. This must be set to a version.")
            }

            if (checkSchema) {
                EPCISDocumentBaseXMLMapper.ValidateEPCISQueryDocumentSchema(xDoc, document.EPCISVersion!!)
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
                val eventType = EPCISDocumentBaseXMLMapper.GetEventTypeFromProfile(x)
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

        val xDoc = EPCISDocumentBaseXMLMapper.WriteXml(doc, epcisNS, "EPCISQueryDocument")
        if (xDoc.root == null) {
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
            var xEvent = OpenTraceabilityXmlMapper.toXml(xname, e, doc.EPCISVersion!!)
            if (e.EventType == EventType.TransformationEvent && doc.EPCISVersion == EPCISVersion.V1) {
                xEvent = Element("extension", xEvent)
            }
            if (xEvent != null) {
                xEventList.add(xEvent)
            }
        }

        EPCISDocumentBaseXMLMapper.ValidateEPCISQueryDocumentSchema(xDoc, doc.EPCISVersion!!)

        return xDoc.toString()
    }

}