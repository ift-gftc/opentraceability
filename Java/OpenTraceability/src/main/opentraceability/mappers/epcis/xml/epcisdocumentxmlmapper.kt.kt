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
            if (document.EpcisVersion == null) {
                throw Exception("doc.epcisVersion is NULL. This must be set to a version.")
            }

            if (checkSchema) {
                EPCISDocumentBaseXMLMapper.ValidateEPCISQueryDocumentSchema(xDoc, document.EpcisVersion!!)
            }

            val epcisQueryXName = if (document.EpcisVersion == EPCISVersion.V1) {
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
                if (document.EpcisVersion == EPCISVersion.V1 && x.element("TransformationEvent") != null) {
                    x = xEvent.element("TransformationEvent")
                }
                val eventType = EPCISDocumentBaseXMLMapper.GetEventTypeFromProfile(x)
                val e = OpenTraceabilityXmlMapper.fromXml(x, eventType, document.EpcisVersion!!)
                document.Events.add(e as IEvent)
            }

            return document
        } catch (ex: Exception) {
            val exception = Exception("Failed to parse the EPCIS document from the XML. xml=$strValue", ex)
            OTLogger.Error(exception)
            throw exception
        }
    }



    fun Map(doc: EPCISQueryDocument): String {
        if (doc.EpcisVersion == null) {
            throw Exception("doc.epcisVersion is NULL. This must be set to a version.")
        }

        val epcisNS = if (doc.EpcisVersion == EPCISVersion.V2) {
            Constants.EPCISQUERY_2_NAMESPACE
        } else {
            Constants.EPCISQUERY_1_NAMESPACE
        }

        val xDoc = EPCISDocumentBaseXMLMapper.WriteXml(doc, epcisNS, "EPCISQueryDocument")
        if (xDoc.root == null) {
            throw Exception("Failed to parse EPCISQueryDocument from xml string because after parsing the XDocument the Root property was null.")
        }

        val epcisQueryXName = if (doc.EpcisVersion == EPCISVersion.V1) {
            Constants.EPCISQUERY_1_XNAMESPACE
        } else {
            Constants.EPCISQUERY_2_XNAMESPACE
        }

        // write the query name
        xDoc.root!!.add(
            XElement("EPCISBody",
                XElement(epcisQueryXName + "QueryResults",
                    XElement("queryName"),
                    XElement("resultsBody",
                        XElement("EventList"))
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
            var xEvent = OpenTraceabilityXmlMapper.toXml(xname, e, doc.EpcisVersion!!)
            if (e.EventType == EventType.TransformationEvent && doc.EpcisVersion == EPCISVersion.V1) {
                xEvent = XElement("extension", xEvent)
            }
            if (xEvent != null) {
                xEventList.add(xEvent)
            }
        }

        EPCISDocumentBaseXMLMapper.ValidateEPCISQueryDocumentSchema(xDoc, doc.EpcisVersion!!)

        return xDoc.toString()
    }

}