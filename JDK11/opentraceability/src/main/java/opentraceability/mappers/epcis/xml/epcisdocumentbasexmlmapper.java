package opentraceability.mappers.epcis.xml;

import opentraceability.interfaces.IEvent;
import opentraceability.mappers.OpenTraceabilityXmlMapper;
import opentraceability.models.common.StandardBusinessDocumentHeader;
import opentraceability.models.events.*;
import opentraceability.utility.StringExtensions;
import opentraceability.utility.XmlSchemaChecker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class EPCISDocumentBaseXMLMapper {

    private static String ISO_DATE_TIME = DateTimeFormatter.ISO_OFFSET_DATE_TIME.toString();

    private static XmlSchemaChecker _schemaChecker = new XmlSchemaChecker();

    public static<T extends EPCISBaseDocument> Pair<T, Document> readXml(String strValue, Class<T> clazz) {
        Document xDoc = StringExtensions.parseXmlToDocument(strValue);

        if (xDoc == null || xDoc.getDocumentElement() == null) {
            throw new Exception("Failed to parse EPCISBaseDocument from xml string because after parsing the XDocument the Root property was null.");
        }

        T document = clazz.getDeclaredConstructor().newInstance();

        for (int i = 0; i < xDoc.getDocumentElement().getAttributes().getLength(); i++) {
            org.w3c.dom.Attr attribute = (org.w3c.dom.Attr) xDoc.getDocumentElement().getAttributes().item(i);

            if (attribute.getName().equals("creationDate") || attribute.getName().equals("schemaVersion")) {
                continue;
            } else {
                if (attribute.getNamespaceURI().equals(opentraceability.Constants.XMLNS_NAMEPSACE)) {
                    document.namespaces.put(attribute.getLocalName(), attribute.getValue());
                } else {
                    document.attributes.put(attribute.getName(), attribute.getValue());
                }
            }
        }

        if (document.namespaces.values().contains(opentraceability.Constants.EPCIS_2_NAMESPACE) ||
                document.namespaces.values().contains(opentraceability.Constants.EPCISQUERY_2_NAMESPACE)) {
            document.epcisVersion = EPCISVersion.V2;
        } else if (document.namespaces.values().contains(opentraceability.Constants.EPCIS_1_NAMESPACE) ||
                document.namespaces.values().contains(opentraceability.Constants.EPCISQUERY_1_NAMESPACE)) {
            document.epcisVersion = EPCISVersion.V1;
        }

        if (document.epcisVersion == null) {
            throw new Exception("Failed to determine the EPCIS version of the XML document. Must contain a namespace with either '" +
                    opentraceability.Constants.EPCIS_2_NAMESPACE + "' or '" + opentraceability.Constants.EPCIS_1_NAMESPACE + "' or '" +
                    opentraceability.Constants.EPCISQUERY_2_NAMESPACE + "' or '" + opentraceability.Constants.EPCISQUERY_1_NAMESPACE + "'");
        }

        String creationDateAttributeStr = xDoc.getDocumentElement().getAttribute("creationDate");
        if (!creationDateAttributeStr.isBlank()) {
            document.creationDate = StringExtensions.tryConvertToDateTimeOffset(creationDateAttributeStr);
        }

        Element xHeader = (Element) xDoc.getDocumentElement().getFirstElementByXPath("EPCISHeader" +
                opentraceability.Constants.SBDH_XNAMESPACE + "StandardBusinessDocumentHeader");
        if (xHeader != null) {
            document.header =
                    OpenTraceabilityXmlMapper.fromXml(xHeader, StandardBusinessDocumentHeader.class, document.epcisVersion);
        }

        Element xMasterData = xDoc.getDocumentElement().getFirstElementByXPath("EPCISHeader/extension/EPCISMasterData");
        if (xMasterData != null) {
            EPCISXmlMasterDataReader.readMasterData(document, xMasterData);
        }
        return new Pair<>(document, xDoc);
    }


    public static<T extends EPCISBaseDocument> Document writeXml(T doc, String epcisNS, String rootEleName) {

        if (doc.epcisVersion == null) {
            throw new Exception("doc.epcisVersion is NULL. This must be set to a version.");
        }

        Document xDoc = createXmlElementNS(epcisNS, rootEleName);

        doc.attributes.entrySet().stream()
                .forEach(p -> xDoc.setAttribute(p.getKey(), p.getValue()));

        doc.namespaces.entrySet().stream()
                .filter(ns -> ns.getValue() != opentraceability.Constants.CBVMDA_NAMESPACE &&
                        ns.getValue() != opentraceability.Constants.EPCISQUERY_1_NAMESPACE &&
                        ns.getValue() != opentraceability.Constants.EPCISQUERY_2_NAMESPACE &&
                        ns.getValue() != opentraceability.Constants.EPCIS_1_NAMESPACE &&
                        ns.getValue() != opentraceability.Constants.EPCIS_2_NAMESPACE)
                .forEach(ns -> xDoc.setAttributeNS(opentraceability.Constants.XMLNS_XNAMESPACE, ns.getKey(), ns.getValue()));

        if (doc.creationDate != null)
        {
            xDoc.setAttribute("creationDate", doc.creationDate.format(DateTimeFormatter.ofPattern(ISO_DATE_TIME)));
        }

        if (doc.epcisVersion == EPCISVersion.V2) {
            xDoc.setAttribute("schemaVersion", "2.0");
            if (doc instanceof EPCISQueryDocument) {
                xDoc.setAttributeNS(opentraceability.Constants.XMLNS_XNAMESPACE, "epcisq", opentraceability.Constants.EPCISQUERY_2_NAMESPACE);
                xDoc.setAttributeNS(opentraceability.Constants.XMLNS_XNAMESPACE, "cbvmda", opentraceability.Constants.CBVMDA_NAMESPACE);
            } else