package opentraceability.mappers.epcis.xml;

import opentraceability.models.events.EPCISQueryDocument;
import opentraceability.models.events.EPCISVersion;
import opentraceability.interfaces.IEvent;
import opentraceability.interfaces.IEPCISQueryDocumentMapper;
import opentraceability.mappers.OpenTraceabilityXmlMapper;
import opentraceability.utility.Constants;
import opentraceability.utility.OTLogger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Objects;

public class EPCISQueryDocumentXMLMapper implements IEPCISQueryDocumentMapper {

    @Override
    public EPCISQueryDocument map(String strValue, boolean checkSchema) throws Exception {
        try {
            var (document, xDoc) = EPCISDocumentBaseXMLMapper.readXml<EPCISQueryDocument>(strValue);

            if (xDoc.getDocumentElement() == null) {
                throw new Exception("Failed to parse EPCISQueryDocument from xml string because after parsing the XDocument the Root property was null.");
            }
            if (Objects.isNull(document.epcisVersion)) {
                throw new Exception("doc.epcisVersion is NULL. This must be set to a version.");
            }

            if (checkSchema) {
                EPCISDocumentBaseXMLMapper.validateEPCISQueryDocumentSchema(xDoc, Objects.requireNonNull(document.epcisVersion));
            }

            String epcisQueryXName = document.epcisVersion.equals(EPCISVersion.V1) ? Constants.EPCISQUERY_1_XNAMESPACE : Constants.EPCISQUERY_2_XNAMESPACE;

            Element xQueryName = xDoc.getDocumentElement().getFirstElementByXPath("EPCISBody/" + epcisQueryXName + ":QueryResults/queryName");

            if (xQueryName != null) {
                document.QueryName = xQueryName.getTextContent();
            }

            Element xEventList = xDoc.getDocumentElement().getFirstElementByXPath("EPCISBody/" + epcisQueryXName + ":QueryResults/resultsBody/EventList");

            if (xEventList != null) {
                for (int i = 0; i < xEventList.getChildNodes().getLength(); i++) {
                    Element xEvent = (Element) xEventList.getChildNodes().item(i);
                    if (xEvent != null) {
                        String eventType = EPCISDocumentBaseXMLMapper.getEventTypeFromProfile(xEvent);
                        IEvent e = OpenTraceabilityXmlMapper.fromXml(xEvent, eventType, Objects.requireNonNull(document.epcisVersion));
                        document.events.add(e);
                    }
                }
            }

            return document;
        } catch (Exception ex) {
            Exception exception = new Exception("Failed to parse the EPCIS document from the XML. xml= " + strValue, ex);
            OTLogger.error(exception);
            throw exception;
        }
    }

    @Override
    public String map(EPCISQueryDocument doc) throws Exception {

        if (Objects.isNull(doc.epcisVersion)) {
            throw new Exception("EPCISVersion is NULL. This must be set to a version.");
        }

        String epcisNS = doc.epcisVersion.equals(EPCISVersion.V2) ? Constants.EPCISQUERY_2_NAMESPACE : Constants.EPCISQUERY_1_NAMESPACE;

        var xDoc = EPCISDocumentBaseXMLMapper.writeXml(doc, epcisNS, "EPCISQueryDocument");

        if (xDoc.getDocumentElement() == null) {
            throw new Exception("Failed to parse EPCISQueryDocument from xml string because after parsing the XDocument the Root property was null.");
        }

        String epcisQueryXName = doc.epcisVersion.equals(EPCISVersion.V1) ? Constants.EPCISQUERY_1_XNAMESPACE : Constants.EPCISQUERY_2_XNAMESPACE;

        Element xQueryResults = xDoc.createElementNS(epcisQueryXName, "QueryResults");
        xQueryResults.appendChild(xDoc.createElement("queryName"));
        Element resultsBody = xDoc.createElement("resultsBody");
        resultsBody.appendChild(xDoc.createElement("EventList"));
        xQueryResults.appendChild(resultsBody);
        Objects.requireNonNull(xDoc.getDocumentElement().getElementsByTagName("EPCISBody")).item(0).appendChild(xQueryResults);

        Element xQueryName = xDoc.getDocumentElement().getFirstElementByXPath("EPCISBody/" + epcisQueryXName + ":QueryResults/queryName");
        if (xQueryName != null) {
            xQueryName.setTextContent(doc.QueryName);
        }

        Element xEventList = xDoc.getDocumentElement().getFirstElementByXPath("EPCISBody/" + epcisQueryXName + ":QueryResults/resultsBody/EventList");

        for (IEvent e : doc.events) {
            String xname = EPCISDocumentBaseXMLMapper.getEventXName(e);
            Element xEvent = OpenTraceabilityXmlMapper.toXml(xname, e, Objects.requireNonNull(doc.epcisVersion));
            if (Objects.requireNonNull(e.eventType).equals(opentraceability.models.events.EventType.TransformationEvent) && doc.epcisVersion == EPCISVersion.V1) {
                Element extension = xDoc.createElement("extension");
                extension.appendChild(xEvent);
                xEventList.appendChild(extension);
            } else {
                if (Objects.nonNull(xEvent)) {
                    xEventList.appendChild(xEvent);
                }
            }
        }

        EPCISDocumentBaseXMLMapper.validateEPCISQueryDocumentSchema(xDoc, Objects.requireNonNull(doc.epcisVersion));

        return docToString(xDoc);
    }

    private String docToString(org.w3c.dom.Document doc) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        javax.xml.transform.Transformer transformer;
        transformer = transformerFactory.newTransformer();
        StringWriter writer;
        writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.getBuffer().toString();
    }
}