package opentraceability.mappers.epcis.xml;

import opentraceability.*;
import opentraceability.interfaces.*;
import opentraceability.mappers.*;
import opentraceability.models.events.*;
import opentraceability.utility.*;


public class EPCISQueryDocumentXMLMapper implements IEPCISQueryDocumentMapper
{
	public EPCISQueryDocument map(String strValue, boolean checkSchema) throws Exception {
		XElement xDoc;
		tangible.OutObject<XElement> tempOut_xDoc = new tangible.OutObject<XElement>();
		EPCISQueryDocument document = EPCISDocumentBaseXMLMapper.ReadXml(strValue, tempOut_xDoc, EPCISQueryDocument.class);
		xDoc = tempOut_xDoc.outArgValue;
		if (xDoc == null)
		{
			throw new RuntimeException("Failed to parse EPCISQueryDocument from xml string because after parsing the XElement the Root property was null.");
		}
		if (document.epcisVersion == null)
		{
			throw new RuntimeException("doc.EPCISVersion is NULL. This must be set to a version.");
		}

		if (checkSchema)
		{
			EPCISDocumentBaseXMLMapper.ValidateEPCISQueryDocumentSchema(xDoc, document.epcisVersion);
		}

		String epcisQueryXName = (document.epcisVersion == EPCISVersion.V1) ? Constants.EPCISQUERY_1_XNAMESPACE : Constants.EPCISQUERY_2_XNAMESPACE;

		// read the query name
		XElement xQueryName = xDoc.Element("EPCISBody/epcisq:QueryResults/queryName");
		if (!xQueryName.IsNull)
		{
			document.QueryName = xQueryName.getValue();
		}

		// read the events
		XElement xEventList = xDoc.Element("EPCISBody/epcisq:QueryResults/resultsBody/EventList");
		if (!xEventList.IsNull)
		{
			for (XElement xEvent : xEventList.Elements())
			{
				XElement x = xEvent;
				if (document.epcisVersion == EPCISVersion.V1 && !x.Element("TransformationEvent").IsNull)
				{
					x = xEvent.Element("TransformationEvent");
				}
				Class eventType = EPCISDocumentBaseXMLMapper.GetEventTypeFromProfile(x);
				IEvent e = (IEvent)OpenTraceabilityXmlMapper.FromXml(x, document.epcisVersion, eventType);
				document.events.add(e);
			}
		}

		return document;
	}

	public String map(EPCISQueryDocument doc) throws Exception {
		if (doc.epcisVersion == null)
		{
			throw new RuntimeException("doc.EPCISVersion is NULL. This must be set to a version.");
		}

		XElement xDoc = EPCISDocumentBaseXMLMapper.WriteXml(doc, "epcisq", "EPCISQueryDocument");
		if (xDoc == null)
		{
			throw new RuntimeException("Failed to parse EPCISQueryDocument from xml string because after parsing the XElement the Root property was null.");
		}

		String epcisQueryXName = (doc.epcisVersion == EPCISVersion.V1) ? Constants.EPCISQUERY_1_XNAMESPACE : Constants.EPCISQUERY_2_XNAMESPACE;

		// write the query name
		xDoc.Add(new XElement("EPCISBody")).Add(new XElement("epcisq:QueryResults"));
		xDoc.Element("EPCISBody/QueryResults").Add(new XElement("queryName"));
		xDoc.Element("EPCISBody/QueryResults").Add(new XElement("resultsBody")).Add(new XElement("EventList"));
		xDoc.FixPrefixesAndNamespacing();

		XElement xQueryName = xDoc.Element("EPCISBody/epcisq:QueryResults/queryName");
		if (!xQueryName.IsNull)
		{
			xQueryName.setValue(doc.QueryName);
		}
		else
		{
			throw new Exception("Failed to find EPCISBody/epcisq:QueryResults/queryName.");
		}

		XElement xEventList = xDoc.Element("EPCISBody/epcisq:QueryResults/resultsBody/EventList");
		if (xEventList.IsNull)
		{
			throw new RuntimeException("Failed to find event EPCISBody/epcisq:QueryResults/resultsBody/EventList.");
		}

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

		xDoc.FixPrefixesAndNamespacing();

		EPCISDocumentBaseXMLMapper.ValidateEPCISQueryDocumentSchema(xDoc, doc.epcisVersion);

		return xDoc.toString();
	}
}
