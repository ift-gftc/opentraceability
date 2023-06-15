package opentraceability.utility;

import org.json.JSONObject;
import org.json.XML;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class OTXmlUtil {

    public static Element createXmlElement(String name) throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        Element rootElement = document.createElement(name);

        return rootElement;
    }

    public static Element createXmlElementNS(String ns, String name) throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        Element rootElement = document.createElementNS(ns, name);

        return rootElement;
    }

    public static void addElement(Element x, Element element) {
        Element importedElement = (Element) x.getOwnerDocument().importNode(element, true);

        x.appendChild(importedElement);

        element.getParentNode().removeChild(element);
    }

    public static Element addElement(Element element, String xName) {
        Element newElement = element.getOwnerDocument().createElement(xName);
        element.appendChild(newElement);
        return newElement;
    }

    public static Element addElementNS(Element element, String ns, String xName) {
        Element newElement = element.getOwnerDocument().createElementNS(ns, xName);
        element.appendChild(newElement);
        return newElement;
    }

    public static Element elementByName(Element element, String nodeName) {
        NodeList nodeList = element.getElementsByTagName(nodeName);
        return nodeList.getLength() > 0 ? (Element) nodeList.item(0) : null;
    }

    public static Element elementByName(Element element, String nsUri, String nodeName) {
        NodeList nodeList = element.getElementsByTagNameNS(nsUri, nodeName);
        return nodeList.getLength() > 0 ? (Element) nodeList.item(0) : null;
    }

    public static ArrayList<Element> getElements(Element element, String nodeName) {
        NodeList nodeList = element.getElementsByTagName(nodeName);
        return nodeListToArrayList(nodeList);
    }

    public static String toXmlString(Element element) throws Exception {
        StringWriter writer = new StringWriter();
        TransformerFactory.newInstance().newTransformer().transform(new DOMSource(element), new StreamResult(writer));
        return writer.getBuffer().toString();
    }

    public static JSONObject toJSON(Element element) {
        String xmlString = null;
        try {
            xmlString = toXmlString(element);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return XML.toJSONObject(xmlString);
    }

    public static Element fromJSON(JSONObject jsonObject) {
        String xmlStr = XML.toString(jsonObject);
        Document doc;
        try {
            doc = XMLParser.parse(xmlStr);
        } catch (Exception e) {
            return null;
        }
        return doc.getDocumentElement();
    }

    public static ArrayList<Element> getElements(Element element, String nsUri, String nodeName) {
        NodeList nodeList = element.getElementsByTagNameNS(nsUri, nodeName);
        return nodeListToArrayList(nodeList);
    }

    public static Element getFirstElementByXPath(Element element, String xpathStr) {
        try {
            XPathFactory xpathFactory = XPathFactory.newInstance();
            javax.xml.xpath.XPath xpath = xpathFactory.newXPath();
            NodeList result = (NodeList) xpath.evaluate(xpathStr, element, XPathConstants.NODESET);

            for (int i = 0; i < result.getLength(); i++) {
                if (result.item(i) instanceof Element) {
                    return (Element) result.item(i);
                }
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static NodeList getElementsByXPath(Element element, String xpathStr) {
        try {
            XPathFactory xpathFactory = XPathFactory.newInstance();
            javax.xml.xpath.XPath xpath = xpathFactory.newXPath();
            return (NodeList) xpath.evaluate(xpathStr, element, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void forEachIndex(NodeList nodeList, ForEachIndexFunction function) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i) instanceof Element) {
                function.forEachIndex((Element) nodeList.item(i), i);
            }
        }
    }

    private static ArrayList<Element> nodeListToArrayList(NodeList nodeList) {
        ArrayList<Element> arrayList = new ArrayList<Element>(nodeList.getLength());
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i) instanceof Element) {
                arrayList.add((Element) nodeList.item(i));
            }
        }
        return arrayList;
    }

    private static final class XMLParser {
        private static final javax.xml.parsers.DocumentBuilderFactory builderFactory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        public static Document parse(String xml) throws Exception {
            return builderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        }
    }

    public interface ForEachIndexFunction {
        void forEachIndex(Element element, int index);
    }
}