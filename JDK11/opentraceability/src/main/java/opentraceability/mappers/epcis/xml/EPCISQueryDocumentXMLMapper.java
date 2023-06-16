package gs1.mappers.epcis;

import opentraceability.*;
import opentraceability.interfaces.*;
import opentraceability.mappers.*;
import opentraceability.mappers.epcis.xml.*;
import opentraceability.models.events.*;
import opentraceability.utility.*;

public class EPCISQueryDocumentXMLMapper implements IEPCISQueryDocumentMapper
{

	public final EPCISQueryDocument Map(String strValue)
	{
		return Map(strValue, true);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public EPCISQueryDocument Map(string strValue, bool checkSchema = true)
	public final EPCISQueryDocument Map(String strValue, boolean checkSchema)
	{
		try
		{
			// TODO: validate the schema depending on the version in the document

			XDocument xDoc;
			tangible.OutObject<XDocument> tempOut_xDoc = new tangible.OutObject<XDocument>();
			EPCISQueryDocument document = EPCISDocumentBaseXMLMapper.<EPCISQueryDocument>ReadXml(strValue, tempOut_xDoc);
		xDoc = tempOut_xDoc.outArgValue;
			if (xDoc.Root == null)
			{
				throw new RuntimeException("Failed to parse EPCISQueryDocument from xml string because after parsing the XDocument the Root property was null.");
			}
			if (document.getEPCISVersion() == null)
			{
				throw new RuntimeException("doc.EPCISVersion is NULL. This must be set to a version.");
			}

			if (checkSchema)
			{
				EPCISDocumentBaseXMLMapper.ValidateEPCISQueryDocumentSchema(xDoc, document.getEPCISVersion());
			}

			XNamespace epcisQueryXName = (document.getEPCISVersion() == EPCISVersion.V1) ? Constants.EPCISQUERY_1_XNAMESPACE : Constants.EPCISQUERY_2_XNAMESPACE;

			// read the query name
			XElement xQueryName = xDoc.Root == null ? null : ((xDoc.Root.Element("EPCISBody") == null ? null : ((xDoc.Root.Element("EPCISBody").Element(epcisQueryXName + "QueryResults") == null ? null : xDoc.Root.Element("EPCISBody").Element(epcisQueryXName + "QueryResults").Element("queryName")))));
			if (xQueryName != null)
			{
				document.setQueryName(xQueryName.getValue());
			}

			// read the events
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: System.Nullable<XElement> xEventList = xDoc.Root == null ? null : ((xDoc.Root.Element("EPCISBody") == null ? null : ((xDoc.Root.Element("EPCISBody").Element(epcisQueryXName + "QueryResults") == null ? null : ((xDoc.Root.Element("EPCISBody").Element(epcisQueryXName + "QueryResults").Element("resultsBody") == null ? null : xDoc.Root.Element("EPCISBody").Element(epcisQueryXName + "QueryResults").Element("resultsBody").Element("EventList"))))))) ?? throw new Exception("Failed to get EPCISBody/EventList after adding it to the XDoc.Root");
			XElement xEventList = xDoc.Root == null ? null : (((xDoc.Root.Element("EPCISBody") == null ? null : ((xDoc.Root.Element("EPCISBody").Element(epcisQueryXName + "QueryResults") == null ? null : ((xDoc.Root.Element("EPCISBody").Element(epcisQueryXName + "QueryResults").Element("resultsBody") == null ? null : xDoc.Root.Element("EPCISBody").Element(epcisQueryXName + "QueryResults").Element("resultsBody").Element("EventList"))))))) != null ? ((xDoc.Root.Element("EPCISBody") == null ? null : ((xDoc.Root.Element("EPCISBody").Element(epcisQueryXName + "QueryResults") == null ? null : ((xDoc.Root.Element("EPCISBody").Element(epcisQueryXName + "QueryResults").Element("resultsBody") == null ? null : xDoc.Root.Element("EPCISBody").Element(epcisQueryXName + "QueryResults").Element("resultsBody").Element("EventList"))))))) : throw new RuntimeException("Failed to get EPCISBody/EventList after adding it to the XDoc.Root"));
			if (xEventList != null)
			{
				for (XElement xEvent : xEventList.Elements())
				{
					XElement x = xEvent;
					if (document.getEPCISVersion() == EPCISVersion.V1 && x.Element("TransformationEvent") != null)
					{
						x = xEvent.Element("TransformationEvent");
					}
					java.lang.Class eventType = EPCISDocumentBaseXMLMapper.GetEventTypeFromProfile(x);
					IEvent e = (IEvent)OpenTraceabilityXmlMapper.FromXml(x, eventType, document.getEPCISVersion());
					document.getEvents().add(e);
				}
			}

			return document;
		}
		catch (RuntimeException Ex)
		{
			RuntimeException exception = new RuntimeException("Failed to parse the EPCIS document from the XML. xml=" + strValue, Ex);
			OTLogger.Error(exception);
			throw Ex;
		}
	}

	public final String Map(EPCISQueryDocument doc)
	{
		if (doc.getEPCISVersion() == null)
		{
			throw new RuntimeException("doc.EPCISVersion is NULL. This must be set to a version.");
		}

		XNamespace epcisNS = (doc.getEPCISVersion() == EPCISVersion.V2) ? Constants.EPCISQUERY_2_NAMESPACE : Constants.EPCISQUERY_1_NAMESPACE;

		XDocument xDoc = EPCISDocumentBaseXMLMapper.WriteXml(doc, epcisNS, "EPCISQueryDocument");
		if (xDoc.Root == null)
		{
			throw new RuntimeException("Failed to parse EPCISQueryDocument from xml string because after parsing the XDocument the Root property was null.");
		}

		XNamespace epcisQueryXName = (doc.getEPCISVersion() == EPCISVersion.V1) ? Constants.EPCISQUERY_1_XNAMESPACE : Constants.EPCISQUERY_2_XNAMESPACE;

		// write the query name
		xDoc.Root.Add(new XElement("EPCISBody", new XElement(epcisQueryXName + "QueryResults", new XElement("queryName"), new XElement("resultsBody", new XElement("EventList")))));

		XElement xQueryName = xDoc.Root == null ? null : ((xDoc.Root.Element("EPCISBody") == null ? null : ((xDoc.Root.Element("EPCISBody").Element(epcisQueryXName + "QueryResults") == null ? null : xDoc.Root.Element("EPCISBody").Element(epcisQueryXName + "QueryResults").Element("queryName")))));
		if (xQueryName != null)
		{
			xQueryName.getValue() = doc.getQueryName();
		}

		// write the events
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: System.Nullable<XElement> xEventList = xDoc.Root == null ? null : ((xDoc.Root.Element("EPCISBody") == null ? null : ((xDoc.Root.Element("EPCISBody").Element(epcisQueryXName + "QueryResults") == null ? null : ((xDoc.Root.Element("EPCISBody").Element(epcisQueryXName + "QueryResults").Element("resultsBody") == null ? null : xDoc.Root.Element("EPCISBody").Element(epcisQueryXName + "QueryResults").Element("resultsBody").Element("EventList"))))))) ?? throw new Exception("Failed to get EPCISBody/EventList after adding it to the XDoc.Root");
		XElement xEventList = xDoc.Root == null ? null : (((xDoc.Root.Element("EPCISBody") == null ? null : ((xDoc.Root.Element("EPCISBody").Element(epcisQueryXName + "QueryResults") == null ? null : ((xDoc.Root.Element("EPCISBody").Element(epcisQueryXName + "QueryResults").Element("resultsBody") == null ? null : xDoc.Root.Element("EPCISBody").Element(epcisQueryXName + "QueryResults").Element("resultsBody").Element("EventList"))))))) != null ? ((xDoc.Root.Element("EPCISBody") == null ? null : ((xDoc.Root.Element("EPCISBody").Element(epcisQueryXName + "QueryResults") == null ? null : ((xDoc.Root.Element("EPCISBody").Element(epcisQueryXName + "QueryResults").Element("resultsBody") == null ? null : xDoc.Root.Element("EPCISBody").Element(epcisQueryXName + "QueryResults").Element("resultsBody").Element("EventList"))))))) : throw new RuntimeException("Failed to get EPCISBody/EventList after adding it to the XDoc.Root"));
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

		EPCISDocumentBaseXMLMapper.ValidateEPCISQueryDocumentSchema(xDoc, doc.getEPCISVersion());

		return xDoc.toString();
	}


	public final Task<EPCISQueryDocument> MapAsync(String strValue)
	{
		return MapAsync(strValue, true);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public Task<EPCISQueryDocument> MapAsync(string strValue, bool checkSchema = true)
	public final Task<EPCISQueryDocument> MapAsync(String strValue, boolean checkSchema)
	{
		return Task.FromResult(Map(strValue, checkSchema));
	}

	public final Task<String> MapAsync(EPCISQueryDocument doc)
	{
		return Task.FromResult(Map(doc));
	}
}
