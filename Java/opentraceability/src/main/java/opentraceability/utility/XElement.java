package opentraceability.utility;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import tangible.StringHelper;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    public XElement(String ns, String name, String value) throws Exception {
        this.element = createXmlElement(ns, name);
        this.IsNull = false;
        this.setValue(value);
    }

    public XElement(String ns, String name, Double value) throws Exception {
        this.element = createXmlElement(ns, name);
        this.IsNull = false;
        this.setValue(Double.toString(value));
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

    private Element createXmlElement(String ns, String name) {
        DocumentBuilder documentBuilder = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            documentBuilder = factory.newDocumentBuilder();

            var document = documentBuilder.newDocument();

            org.w3c.dom.Element ele;
            if (tangible.StringHelper.isNullOrEmpty(ns)) {
                ele = document.createElement(name);
            } else {
                ele = document.createElementNS(ns, name);
            }
            document.appendChild(ele);
            return document.getDocumentElement();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public XElement DeepClone()
    {
        DocumentBuilder documentBuilder = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            documentBuilder = factory.newDocumentBuilder();

            var document = documentBuilder.newDocument();
            var ele = (Element)document.importNode(this.element, true);
            document.appendChild(ele);
            XElement xele = new XElement(ele);
            this.copyNamespacesTo(xele);
            return xele;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
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

        xele.element = (Element) this.element.getOwnerDocument().importNode(xele.element, true);
        this.element.appendChild(xele.element);
        return xele;
    }

    public String getValue()
    {
        if (this.IsNull)
        {
            return "";
        }
        else
        {
            return StringExtensions.trimString(this.element.getTextContent());
        }
    }

    public Boolean IsEmpty()
    {
        if (this.element.getChildNodes().getLength() == 0 && this.element.getAttributes().getLength() == 0)
        {
            return true;
        }
        return false;
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

    public void AddNamespace(String prefix, String uri)
    {
        this.element.getOwnerDocument().getDocumentElement().setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + prefix, uri);
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
        // return empty if is null, so we can chain...
        if (IsNull)
        {
            return new XElement();
        }

        var result = this.queryByXpath(xpath);

        if (result.getLength() == 0)
        {
            return new XElement();
        }
        else
        {
            return new XElement((Element)result.item(0));
        }
    }

    public XElement Element(String namespaceURI, String nodeName) throws XPathExpressionException
    {
        // return empty if is null, so we can chain...
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
        // return empty if is null, so we can chain...
        if (IsNull)
        {
            return new ArrayList<XElement>();
        }

        var result = this.queryByXpath(xpath);

        ArrayList<XElement> xelements = new ArrayList<>();
        for (int i = 0; i < result.getLength(); i++)
        {
            XElement xe = new XElement((Element) result.item(i));
            xelements.add(xe);
        }
        return xelements;
    }

    private NodeList queryByXpath(String xpath) throws XPathExpressionException {
        try
        {
            Map prefixAndNamespacesMap = this.GetNamespacesAndPrefixesMap();
            List<String> prefixes = this.GetPrefixes();
            SimpleNamespaceContext nsContext = new SimpleNamespaceContext(prefixAndNamespacesMap, prefixes);

            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpathObj = xpathFactory.newXPath();
            xpathObj.setNamespaceContext(nsContext);

            XPathExpression expr = xpathObj.compile(xpath);
            NodeList result = (NodeList) expr.evaluate(this.element, XPathConstants.NODESET);
            return result;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    public Map<String, String> GetNamespacesAndPrefixesMap()
    {
        XElement xe = new XElement(this.element.getOwnerDocument().getDocumentElement());
        HashMap<String, String> map = new HashMap();
        for (XAttribute xatt: xe.Attributes())
        {
            if (xatt.Name.startsWith("xmlns:"))
            {
                String prefix = ListExtensions.LastOrDefault(Arrays.stream(xatt.Name.split(":")));
                String namespace = xatt.Value;

                map.put(prefix, namespace);
                map.put(namespace, prefix);
            }
        }
        return map;
    }

    public List<String> GetPrefixes()
    {
        XElement xe = new XElement(this.element.getOwnerDocument().getDocumentElement());
        List<String> prefixes = new ArrayList<>();
        for (XAttribute xatt: xe.Attributes())
        {
            if (xatt.Name.startsWith("xmlns:"))
            {
                String prefix = ListExtensions.LastOrDefault(Arrays.stream(xatt.Name.split(":")));
                prefixes.add(prefix);
            }
        }
        return prefixes;
    }

    public static XElement Parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        var documentBuilder = factory.newDocumentBuilder();
        ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        var document = documentBuilder.parse(input);

        XElement e = new XElement(document.getDocumentElement());
        return e;
    }

    public String Attribute(String name)
    {
        if (IsNull)
        {
            return null;
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

    public String Attribute(String ns, String name) {
        if (IsNull)
        {
            return null;
        }

        return this.element.getAttributeNS(ns, name);
    }

    public ArrayList<XAttribute> Attributes() {
        ArrayList<XAttribute> xAtts = new ArrayList<>();
        if (!IsNull)
        {
            var attribtues = this.element.getAttributes();
            for (int i = 0; i < attribtues.getLength(); i++)
            {
                var a = attribtues.item(i);
                XAttribute xAtt = new XAttribute(a.getNamespaceURI(), a.getNodeName(), a.getLocalName(), a.getNodeValue());
                xAtts.add(xAtt);
            }
        }
        return xAtts;
    }

    public void SetAttributeValue(String namespaceUri, String name, Object o)
    {
        XAttribute xatt = new XAttribute(namespaceUri, name, o.toString());
        this.Add(xatt);
    }

    public ArrayList<XElement> Elements() {
        if (this.IsNull)
        {
            return new ArrayList<>();
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

    public void setValue(String value)  {
        if (this.IsNull)
        {
            return;
        }

        this.element.setTextContent(value);
    }

    public String getNamespaceUri() {

        if (this.IsNull)
        {
            return null;
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

    public String getTagName() {
        if (this.IsNull)
        {
            return null;
        }

        return this.element.getTagName();
    }

    @Override
    public String toString() {
        try
        {
            // clone elements
            XElement clone = this.DeepClone();

            DOMSource domSource = new DOMSource(clone.element.getOwnerDocument());
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(domSource, result);
            String str = writer.toString();
            return str;
        }
        catch (TransformerException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void copyNamespacesTo(XElement xele) {
        if (!this.IsNull)
        {
            var root = new XElement(this.element.getOwnerDocument().getDocumentElement());
            var targetRoot = new XElement(xele.element.getOwnerDocument().getDocumentElement());
            for (XAttribute att: root.Attributes())
            {
                if (att.Name.startsWith("xmlns:"))
                {
                    String existing = targetRoot.Attribute(att.Name);
                    if (StringHelper.isNullOrEmpty(existing))
                    {
                        targetRoot.element.setAttributeNS("http://www.w3.org/2000/xmlns/", att.Name, att.Value);
                    }
                }
            }
        }
    }

    public void RemoveNamespace(String prefix)
    {
        var root = this.element.getOwnerDocument().getDocumentElement();
        root.removeAttribute("xmlns:" + prefix);
    }

    public void FixPrefixesAndNamespacing()
    {
        var namespaces = GetNamespacesAndPrefixesMap();
        UpdatePrefixes_Internal(this, namespaces);
    }

    private void UpdatePrefixes_Internal(XElement xe, Map<String, String> namespaces)
    {
        String tag = xe.getTagName();
        String namespaceURI = xe.getNamespaceUri();
        if (!tag.contains(":") && !StringHelper.isNullOrEmpty(namespaceURI))
        {
            String prefix = namespaces.get(namespaceURI);
            if (!StringHelper.isNullOrEmpty(prefix))
            {
                xe.changeTagName(null,prefix + ":" + tag);
            }
        }

        else if (tag.contains(":") && StringHelper.isNullOrEmpty(namespaceURI))
        {
            String prefix = ListExtensions.FirstOrDefault(Arrays.stream(tag.split(":")));
            String ns = namespaces.get(prefix);
            if (!StringHelper.isNullOrEmpty(ns))
            {
                xe.changeTagName(ns, tag);
            }
        }

        for (XElement xchild: xe.Elements())
        {
            xchild.UpdatePrefixes_Internal(xchild, namespaces);
        }
    }

    private void changeTagName(String ns, String newTagName) {
        Element newElement;
        if (StringHelper.isNullOrEmpty(ns))
        {
            newElement = element.getOwnerDocument().createElement(newTagName);
        }
        else
        {
            newElement = element.getOwnerDocument().createElementNS(ns, newTagName);
        }

        // Transfer the child nodes to the new element
        NodeList childNodes = element.getChildNodes();
        while (childNodes.getLength() > 0) {
            Node child = childNodes.item(0);
            element.removeChild(child);
            newElement.appendChild(child);
        }

        // Transfer the attributes to the new element
        element.removeAttribute("xmlns"); // Remove the default namespace attribute if present
        while (element.hasAttributes()) {
            Node attribute = element.getAttributes().item(0);
            element.removeAttributeNode((org.w3c.dom.Attr) attribute);
            newElement.setAttributeNode((org.w3c.dom.Attr) attribute);
        }

        // Replace the original element with the new element in its parent node
        Node parent = element.getParentNode();
        parent.replaceChild(newElement, element);
        element = newElement;
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
