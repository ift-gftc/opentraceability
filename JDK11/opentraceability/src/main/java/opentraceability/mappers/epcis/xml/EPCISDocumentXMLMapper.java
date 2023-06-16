package gs1.mappers.epcis;

import opentraceability.*;
import opentraceability.interfaces.*;
import opentraceability.mappers.*;
import opentraceability.mappers.epcis.xml.*;
import opentraceability.models.events.*;
import opentraceability.utility.*;

public class EPCISDocumentXMLMapper implements IEPCISDocumentMapper
{
	public final EPCISDocument Map(String strValue)
	{
		try
		{
			XDocument xDoc;
			tangible.OutObject<XDocument> tempOut_xDoc = new tangible.OutObject<XDocument>();
			EPCISDocument doc = EPCISDocumentBaseXMLMapper.<EPCISDocument>ReadXml(strValue, tempOut_xDoc);
		xDoc = tempOut_xDoc.outArgValue;

			if (doc.getEPCISVersion() == null)
			{
				throw new RuntimeException("doc.EPCISVersion is NULL. This must be set to a version.");
			}

			EPCISDocumentBaseXMLMapper.ValidateEPCISDocumentSchema(xDoc, doc.getEPCISVersion());

			// read the events
			XElement xEventList = xDoc.Root == null ? null : ((xDoc.Root.Element("EPCISBody") == null ? null : xDoc.Root.Element("EPCISBody").Element("EventList")));
			if (xEventList != null)
			{
				for (XElement xEvent : xEventList.Elements())
				{
					java.lang.Class eventType = EPCISDocumentBaseXMLMapper.GetEventTypeFromProfile(xEvent);
					IEvent e = (IEvent)OpenTraceabilityXmlMapper.FromXml(xEvent, eventType, doc.getEPCISVersion());
					doc.getEvents().add(e);
				}
			}

			return doc;
		}
		catch (RuntimeException Ex)
		{
			RuntimeException exception = new RuntimeException("Failed to parse the EPCIS document from the XML. xml=" + strValue, Ex);
			OTLogger.Error(exception);
			throw Ex;
		}
	}

	public final String Map(EPCISDocument doc)
	{
		if (doc.getEPCISVersion() == null)
		{
			throw new RuntimeException("doc.EPCISVersion is NULL. This must be set to a version.");
		}

		XNamespace epcisNS = (doc.getEPCISVersion() == EPCISVersion.V2) ? Constants.EPCIS_2_NAMESPACE : Constants.EPCIS_1_NAMESPACE;

		XDocument xDoc = EPCISDocumentBaseXMLMapper.WriteXml(doc, epcisNS, "EPCISDocument");
		if (xDoc.Root == null)
		{
			throw new RuntimeException("Failed to parse EPCISQueryDocument from xml string because after parsing the XDocument the Root property was null.");
		}

		// write the events
		xDoc.Root.Add(new XElement("EPCISBody", new XElement("EventList")));
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: XElement xEventList = xDoc.Root == null ? null : ((xDoc.Root.Element("EPCISBody") == null ? null : xDoc.Root.Element("EPCISBody").Element("EventList"))) ?? throw new Exception("Failed to get EPCISBody/EventList after adding it to the XDoc.Root");
		XElement xEventList = xDoc.Root == null ? null : (((xDoc.Root.Element("EPCISBody") == null ? null : xDoc.Root.Element("EPCISBody").Element("EventList"))) != null ? ((xDoc.Root.Element("EPCISBody") == null ? null : xDoc.Root.Element("EPCISBody").Element("EventList"))) : throw new RuntimeException("Failed to get EPCISBody/EventList after adding it to the XDoc.Root"));
		for (IEvent e : doc.getEvents())
		{
			String xname = EPCISDocumentBaseXMLMapper.GetEventXName(e);
			XElement xEvent = OpenTraceabilityXmlMapper.ToXml(xname, e, doc.getEPCISVersion());
			if (e.getEventType() == EventType.TransformationEvent && doc.getEPCISVersion() == EPCISVersion.V1)
			{
				xEvent = new XElement("extension", xEvent);
			}
			if (xEvent != null)
			{
				xEventList.Add(xEvent);
			}
		}

		EPCISDocumentBaseXMLMapper.ValidateEPCISDocumentSchema(xDoc, doc.getEPCISVersion());

		return xDoc.toString();
	}

	public final Task<EPCISDocument> MapAsync(String strValue)
	{
		return Task.FromResult(Map(strValue));
	}

	public final Task<String> MapAsync(EPCISDocument doc)
	{
		return Task.FromResult(Map(doc));
	}
}
