package opentraceability.mappers.epcis.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import opentraceability.Constants;
import opentraceability.OTLogger;
import opentraceability.interfaces.IEPCISDocumentMapper;
import opentraceability.interfaces.IEvent;
import opentraceability.mappers.OpenTraceabilityXmlMapper;
import opentraceability.models.events.EPCISDocument;
import opentraceability.models.events.EPCISVersion;
import opentraceability.models.events.EventType;
import opentraceability.models.events.IEPCISEvent;
import opentraceability.utility.EPCISDocumentBaseXMLMapper;

public class EPCISDocumentXMLMapper implements IEPCISDocumentMapper {

	@Override
	public EPCISDocument map(String strValue) {
		EPCISDocument document = new EPCISDocument();
		Document d = null;

		try {
			Object[] temp = EPCISDocumentBaseXMLMapper.readXml(strValue, EPCISDocument.class);

			if (temp != null) {
				document = (EPCISDocument) temp[0];
				d = (Document)temp[1];
			}

			Element xDoc = d.getDocumentElement();

			if (document.epcisVersion == null) {
				throw new Exception("doc.epcisVersion is NULL. This must be set to a version.");
			}

			EPCISDocumentBaseXMLMapper.validateEPCISQueryDocumentSchema(xDoc.getOwnerDocument(), document.epcisVersion);

			String epcisQueryXName;
			if (document.epcisVersion == EPCISVersion.V1) {
				epcisQueryXName = Constants.EPCISQUERY_1_XNAMESPACE;
			} else {
				epcisQueryXName = Constants.EPCISQUERY_2_XNAMESPACE;
			}

			// read the events
			Element xEventList = xDoc.getElementsByTagName("EPCISBody").item(0).getElementsByTagName(epcisQueryXName + "QueryResults").item(0)
					.getElementsByTagName("resultsBody").item(0).getElementsByTagName("EventList").item(0);

			int count = xEventList.getChildNodes().getLength();
			Node xEvent;			
			for (int i=0; i<count; i++) {
				xEvent = xEventList.getChildNodes().item(i);
				if (xEvent != null && document.epcisVersion == EPCISVersion.V1 && xEvent.getOwnerDocument().getElementsByTagName("TransformationEvent").item(0) != null) {
					xEvent = xEvent.getOwnerDocument().getElementsByTagName("TransformationEvent").item(0);
				}
				EventType eventType = EPCISDocumentBaseXMLMapper.getEventTypeFromProfile(xEvent);
				IEPCISEvent e = OpenTraceabilityXmlMapper.fromXml(xEvent, eventType, document.epcisVersion);
				document.events.add(e);
			}

			return document;
		} catch (Exception ex) {
			Exception exception = new Exception("Failed to parse the EPCIS document from the XML. xml=" + strValue, ex);
			OTLogger.error(exception);
			throw exception;
		}
	}

	@Override
	public String map(EPCISDocument doc) {
		if (doc.epcisVersion == null) {
			throw new Exception("doc.epcisVersion is NULL. This must be set to a version.");
		}

		String epcisNS;
		if (doc.epcisVersion == EPCISVersion.V2) {
			epcisNS = Constants.EPCISQUERY_2_NAMESPACE;
		} else {
			epcisNS = Constants.EPCISQUERY_1_NAMESPACE;
		}

		Element xDoc = EPCISDocumentBaseXMLMapper.writeXml(doc, epcisNS, "EPCISQueryDocument").getDocumentElement();

		String epcisQueryXName;
		if (doc.epcisVersion == EPCISVersion.V1) {
			epcisQueryXName = Constants.EPCISQUERY_1_XNAMESPACE;
		} else {
			epcisQueryXName = Constants.EPCISQUERY_2_XNAMESPACE;
		}

		// write the query name
		Element xQueryResults = xDoc.appendChild(xDoc.getOwnerDocument().createElement("EPCISBody")).appendChild(xDoc.getOwnerDocument().createElementNS(epcisQueryXName, "QueryResults"));
		xQueryResults.appendChild(xQueryResults.getOwnerDocument().createElement("queryName"));
		
		Element xEventList = xQueryResults.appendChild(xQueryResults.getOwnerDocument().createElement("resultsBody")).appendChild(xQueryResults.getOwnerDocument().createElement("EventList"));
		
		
		for (IEvent e : doc.events) {
			String xname = EPCISDocumentBaseXMLMapper.getEventXName(e);
			Element xEvent = OpenTraceabilityXmlMapper.toXml(xname, e, doc.epcisVersion);
			if (e.getEventType() == EventType.TransformationEvent && doc.epcisVersion == EPCISVersion.V1) {
				Element outerExtensionXml = EPCISDocumentBaseXMLMapper.createXmlElement("extension", xDoc.getOwnerDocument());
				outerExtensionXml.appendChild(xEvent);
				xEvent = outerExtensionXml;
			}
			if (xEvent != null) {
				xEventList.appendChild(xEvent);
			}
		}

		EPCISDocumentBaseXMLMapper.validateEPCISQueryDocumentSchema(xDoc.getOwnerDocument(), doc.epcisVersion);

		return EPCISDocumentBaseXMLMapper.XML2String(xDoc);
	}
}