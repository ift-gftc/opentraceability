package opentraceability.mappers.epcis.xml;

import opentraceability.*;
import opentraceability.interfaces.*;
import opentraceability.mappers.*;
import opentraceability.mappers.epcis.xml.*;
import opentraceability.models.events.*;
import opentraceability.utility.*;

import javax.xml.xpath.XPathExpressionException;


public class EPCISDocumentXMLMapper implements IEPCISDocumentMapper
{
	public EPCISDocument map(String strValue) throws Exception {
			XElement xDoc;
			tangible.OutObject<XElement> tempOut_xDoc = new tangible.OutObject<XElement>();
			EPCISDocument doc = EPCISDocumentBaseXMLMapper.ReadXml(strValue, tempOut_xDoc, EPCISDocument.class);
			xDoc = tempOut_xDoc.outArgValue;

			if (doc.epcisVersion == null)
			{
				throw new RuntimeException("doc.EPCISVersion is NULL. This must be set to a version.");
			}

			EPCISDocumentBaseXMLMapper.ValidateEPCISDocumentSchema(xDoc, doc.epcisVersion);

			// read the events
			XElement xEventList = xDoc.Element("EPCISBody/EventList");
			if (!xEventList.IsNull)
			{
				for (XElement xEvent : xEventList.Elements())
				{
					Class eventType = EPCISDocumentBaseXMLMapper.GetEventTypeFromProfile(xEvent);
					IEvent e = (IEvent)OpenTraceabilityXmlMapper.FromXml(xEvent, doc.epcisVersion, eventType);
					doc.events.add(e);
				}
			}

			return doc;
	}

	public String map(EPCISDocument doc) throws Exception {
		if (doc.epcisVersion == null)
		{
			throw new RuntimeException("doc.EPCISVersion is NULL. This must be set to a version.");
		}

		XElement xDoc = EPCISDocumentBaseXMLMapper.WriteXml(doc, "epcis", "EPCISDocument");
		if (xDoc == null)
		{
			throw new RuntimeException("Failed to parse EPCISQueryDocument from xml string because after parsing the XElement the Root property was null.");
		}

		// write the events
		xDoc.Add(new XElement("EPCISBody", new XElement("EventList")));
		XElement xEventList = xDoc.Element("EPCISBody/EventList");
		for (IEvent e : doc.events)
		{
			String xname = EPCISDocumentBaseXMLMapper.GetEventXName(e);
			XElement xEvent = OpenTraceabilityXmlMapper.ToXml(null, xname, e, doc.epcisVersion, false);
			if (e.eventType == EventType.TransformationEvent && doc.epcisVersion == EPCISVersion.V1)
			{
				xEvent = new XElement("extension", xEvent);
			}
			if (xEvent != null)
			{
				xEventList.Add(xEvent);
			}
		}

		EPCISDocumentBaseXMLMapper.ValidateEPCISDocumentSchema(xDoc, doc.epcisVersion);

		return xDoc.toString();
	}
}
