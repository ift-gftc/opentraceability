package opentraceability.utility;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/*
    My custom element class for working with xml.
 */
public class XElement
{
    public Element element = null;
    public boolean IsNull = true;

    public XElement(String name) throws Exception {
        this.element = createXmlElement(null, name);
        this.IsNull = false;
    }

    public XElement(String ns, String name) throws Exception {
        this.element = createXmlElement(ns, name);
        this.IsNull = false;
    }

    public XElement(String name, XElement child) throws Exception {
        this.element = createXmlElement(null, name);
        this.IsNull = false;
        this.Add(child);
    }

    public XElement(String name, XElement child, XElement child2) throws Exception {
        this.element = createXmlElement(null, name);
        this.IsNull = false;
        this.Add(child);
        this.Add(child2);
    }

    public XElement(String ns, String name, XElement child) throws Exception
    {
        this.element = createXmlElement(ns, name);
        this.IsNull = false;
        this.Add(child);
    }

    public XElement(String ns, String name, XElement child, XElement child2) throws Exception
    {
        this.element = createXmlElement(ns, name);
        this.IsNull = false;
        this.Add(child);
        this.Add(child2);
    }

    public XElement(Element e)
    {
        this.element = e;
        this.IsNull = false;
    }

    public XElement()
    {
        this.IsNull = true;
    }

    public XElement(String name, Double value) throws Exception {
        this.element = createXmlElement(null, name);
        this.IsNull = false;
        this.element.setNodeValue(value.toString());
    }

    private Element createXmlElement(String ns, String name) throws Exception
    {
        var documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        var document = documentBuilder.newDocument();

        org.w3c.dom.Element ele;
        if (tangible.StringHelper.isNullOrEmpty(ns))
        {
            ele = document.createElement(name);
        }
        else
        {
            ele = document.createElementNS(ns, name);
        }
        return ele;
    }

    public XElement Add(String name) throws Exception {
        if (this.IsNull)
        {
            throw new Exception("Cannot Add(name) because is null.");
        }

        Element e = this.element.getOwnerDocument().createElement(name);
        this.element.appendChild(e);
        return new XElement(e);
    }

    public XElement Add(XElement xele) throws Exception {
        if (this.IsNull)
        {
            throw new Exception("Cannot Add(XElement) because is null.");
        }

        this.element.appendChild(xele.element);
        return xele;
    }

    public String getValue()
    {
        if (this.IsNull) return null;
        else return this.element.getNodeValue();
    }

    public XElement Add(XAttribute xAtt)
    {
        if (xAtt != null)
        {
            if (xAtt.Namespace != null)
            {
                this.element.setAttributeNS(xAtt.Namespace, xAtt.Name, xAtt.Value);
            }
            else
            {
                this.element.setAttribute(xAtt.Name, xAtt.Value);
            }
        }
        return this;
    }

    public XElement Add(String ns, String name) throws Exception {
        if (this.IsNull)
        {
            throw new Exception("Cannot Add(ns,name) because is null.");
        }

        Element e = this.element.getOwnerDocument().createElementNS(ns, name);
        this.element.appendChild(e);
        return new XElement(e);
    }

    public XElement Element(String xpath) throws XPathExpressionException
    {
        // return empty if is null so we can chain...
        if (IsNull)
        {
            return new XElement();
        }

        var xpathFactory = XPathFactory.newInstance();
        var xp = xpathFactory.newXPath();
        var result = (NodeList)xp.evaluate(xpath, this.element, XPathConstants.NODESET);

        if (result.getLength() == 0)
        {
            return null;
        }
        else
        {
            return new XElement((Element)result.item(0));
        }
    }

    public XElement Element(String namespaceURI, String nodeName) throws XPathExpressionException
    {
        // return empty if is null so we can chain...
        if (IsNull)
        {
            return new XElement();
        }

        var e = this.element.getElementsByTagNameNS(namespaceURI, nodeName);

        if (e.getLength() == 0)
        {
            return new XElement();
        }
        else
        {
            return new XElement((Element)e.item(0));
        }
    }

    public ArrayList<XElement> Elements(String xpath) throws XPathExpressionException
    {
        // return empty if is null so we can chain...
        if (IsNull)
        {
            return new ArrayList<XElement>();
        }

        var xpathFactory = XPathFactory.newInstance();
        var xp = xpathFactory.newXPath();
        var result = (NodeList) xp.evaluate(xpath, this.element, XPathConstants.NODESET);

        ArrayList<XElement> xelements = new ArrayList<>();
        for (int i = 0; i < result.getLength(); i++)
        {
            XElement xe = new XElement((Element) result.item(i));
            xelements.add(xe);
        }
        return xelements;
    }

    public static XElement Parse(String xml) throws Exception {
        var documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        var document = documentBuilder.parse(input);

        XElement e = new XElement(document.getDocumentElement());
        return e;
    }

    public String Attribute(String name) throws Exception
    {
        if (IsNull)
        {
            throw new Exception("Cannot access attribute(name) on null XElement.");
        }

        return this.element.getAttribute(name);
    }

    public OffsetDateTime AttributeOffsetDateTime(String name) throws Exception {
        String val = this.Attribute(name);
        if (!tangible.StringHelper.isNullOrEmpty(val))
        {
            OffsetDateTime dt = OffsetDateTime.parse(val, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            return dt;
        }
        else {
            return null;
        }
    }

    public String Attribute(String ns, String name) throws Exception {
        if (IsNull)
        {
            throw new Exception("Cannot access attribute(ns,name) on null XElement.");
        }

        return this.element.getAttributeNS(ns, name);
    }

    public ArrayList<XAttribute> Attributes() throws Exception {
        if (IsNull)
        {
            throw new Exception("Cannot access attributes() on null XElement.");
        }

        ArrayList<XAttribute> xAtts = new ArrayList<>();
        var attribtues = this.element.getAttributes();
        for (int i = 0; i < attribtues.getLength(); i++)
        {
            var a = attribtues.item(i);
            XAttribute xAtt = new XAttribute(a.getNamespaceURI(), a.getNodeName(), a.getLocalName(), a.getNodeValue());
            xAtts.add(xAtt);
        }
        return xAtts;
    }

    public void SetAttributeValue(String namespaceUri, String name, Object o)
    {
        XAttribute xatt = new XAttribute(namespaceUri, name, o.toString());
        this.Add(xatt);
    }

    public ArrayList<XElement> Elements() throws Exception {
        if (this.IsNull)
        {
            throw new Exception("Cannot call Elements() on null.");
        }
        ArrayList<XElement> eles = new ArrayList<>();
        var children = this.element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
        {
            var child = children.item(i);
            if (child instanceof Element)
            {
                eles.add(new XElement((Element)children.item(i)));
            }
        }
        return eles;
    }

    public void setValue(String value) throws Exception {
        if (this.IsNull)
        {
            throw new Exception("Cannot call setValue(value) on null XElement.");
        }

        this.element.setNodeValue(value);
    }

    public String getNamespaceUri() throws Exception {

        if (this.IsNull)
        {
            throw new Exception("Cannot call getNamespaceURI() on null XElement.");
        }

        return this.element.getNamespaceURI();
    }

    public String getLocalName() throws Exception {
        if (this.IsNull)
        {
            throw new Exception("Cannot call getLocalName() on null XElement.");
        }

        return this.element.getLocalName();
    }

    public String getNodeValue() throws Exception {
        if (this.IsNull)
        {
            throw new Exception("Cannot call getNodeValue() on null XElement.");
        }

        return this.element.getNodeValue();
    }

    public boolean HasElements() {
        if (this.IsNull) return false;
        else return this.element.hasChildNodes();
    }

    public void SetAttributeValue(String name, String value)
    {
        XAttribute xatt = new XAttribute(name, value);
        this.Add(xatt);
    }

    public String getTagName() throws Exception {
        if (this.IsNull)
        {
            throw new Exception("Cannot call getTagName() on null XElement.");
        }

        return this.element.getTagName();
    }
//
//
//    // Gets the child element using the matching namespace URI (ns) and node name (xName)
//    fun Element.element(namespaceUri: String, nodeName: String): Element? {
//    val nodeList = getElementsByTagNameNS(namespaceUri, nodeName)
//    return if (nodeList.length > 0) nodeList.item(0) as Element else null
//}
//
//    fun Element.elements(nodeName: String): ArrayList<Element> {
//    val nodeList = getElementsByTagName(nodeName)
//    return nodeList.toArrayList()
//}
//
//    fun Element.elements(): ArrayList<Element> {
//    val array = this.childNodes.toArrayList()
//    return array
//}
//
//    fun Element.toXmlString(): String {
//    val transformerFactory = TransformerFactory.newInstance()
//    val transformer = transformerFactory.newTransformer()
//    val source = DOMSource(this)
//    val result = StreamResult(StringWriter())
//    transformer.transform(source, result)
//    return result.writer.toString()
//}
//
//    fun Element.toJSON(): JSONObject {
//    val jsonObj = XML.toJSONObject(this.toXmlString())
//    return jsonObj
//}
//
//    fun JSONObject.toXML(): Element {
//    val xmlStr = XML.toString(this)
//    var xml = xmlStr.parseXmlToDocument()
//    return xml.documentElement
//}
//
//    fun Element.elements(namespaceUri: String, nodeName: String): ArrayList<Element> {
//    val nodeList = getElementsByTagNameNS(namespaceUri, nodeName)
//    return nodeList.toArrayList()
//}
//
//    // Extension function to convert NodeList to ArrayList<Element>
//    fun NodeList.toArrayList(): ArrayList<Element> {
//    val list = ArrayList<Element>(length)
//    for (i in 0 until length) {
//        val item = item(i)
//        if (item is Element) {
//            list.add(item)
//        }
//    }
//    return list
//}
//
//
//    fun Element.getFirstElementByXPath(xpathStr: String): Element? {
//    try {
//
//    } catch (e: XPathExpressionException) {
//        // Handle XPath expression exception
//    }
//    return null
//}
//
//    fun Element.getElementsByXPath(xpathStr: String): NodeList? {
//    try {
//        val xpathFactory = XPathFactory.newInstance()
//        val xpath = xpathFactory.newXPath()
//        return xpath.evaluate(xpathStr, this, XPathConstants.NODESET) as NodeList
//    } catch (e: XPathExpressionException) {
//        // Handle XPath expression exception
//    }
//    return null
//}
//
//    fun NodeList.forEachIndex(action: (element: Element, index: Int) -> Unit) {
//    for (i in 0 until length) {
//        val node = item(i)
//        if (node is Element) {
//            action(node, i)
//        }
//    }
//    }
}
