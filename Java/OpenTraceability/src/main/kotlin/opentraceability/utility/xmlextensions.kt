package opentraceability.utility

import opentraceability.mappers.epcis.utils
import org.json.JSONObject
import org.json.XML
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpressionException
import javax.xml.xpath.XPathFactory
import org.xml.sax.InputSource
import java.io.StringReader
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

fun createXmlElement(name: String): Element {
    val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val document: Document = documentBuilder.newDocument()

    // Create the root element
    val rootElement: Element = document.createElement(name)

    // You can further customize the element by setting attributes, adding child nodes, etc.

    return rootElement
}

fun createXmlElementNS(ns: String, name: String): Element {
    val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val document: Document = documentBuilder.newDocument()

    // Create the root element
    val rootElement: Element = document.createElementNS(ns, name)

    // You can further customize the element by setting attributes, adding child nodes, etc.

    return rootElement
}

fun Element.addElement(x: Element)
{
    // Import the element from the source document to the target document
    val importedElement: Element = this.ownerDocument.importNode(x, true) as Element

    // Append the imported element to the target document
    this.appendChild(importedElement)

    // Remove the element from the source document
    x.parentNode?.removeChild(x)
}

fun Element.addElement(xName: String): Element
{
    val newElement: Element = this.ownerDocument.createElement(xName)
    this.appendChild(newElement)
    return newElement
}

fun Element.addElementNS(ns: String, xName: String): Element
{
    val newElement: Element = this.ownerDocument.createElementNS(ns, xName)
    this.appendChild(newElement)
    return newElement
}

fun Element.element(nodeName: String): Element?
{
    val nodeList = getElementsByTagName(nodeName)
    return if (nodeList.length > 0) nodeList.item(0) as Element else null
}

// Gets the child element using the matching namespace URI (ns) and node name (xName)
fun Element.element(namespaceUri: String, nodeName: String): Element? {
    val nodeList = getElementsByTagNameNS(namespaceUri, nodeName)
    return if (nodeList.length > 0) nodeList.item(0) as Element else null
}

fun Element.elements(nodeName: String): ArrayList<Element> {
    val nodeList = getElementsByTagName(nodeName)
    return nodeList.toArrayList()
}

fun Element.elements(): ArrayList<Element> {
    val array = this.childNodes.toArrayList()
    return array
}

fun Element.toXmlString(): String {
    val transformerFactory = TransformerFactory.newInstance()
    val transformer = transformerFactory.newTransformer()
    val source = DOMSource(this)
    val result = StreamResult(StringWriter())
    transformer.transform(source, result)
    return result.writer.toString()
}

fun Element.toJSON(): JSONObject {
    val jsonObj = XML.toJSONObject(this.toXmlString())
    return jsonObj
}

fun JSONObject.toXML(): Element {
    val xmlStr = XML.toString(this)
    var xml = utils.parseXml(xmlStr)
    return xml.documentElement
}

fun Element.elements(namespaceUri: String, nodeName: String): ArrayList<Element> {
    val nodeList = getElementsByTagNameNS(namespaceUri, nodeName)
    return nodeList.toArrayList()
}

// Extension function to convert NodeList to ArrayList<Element>
fun NodeList.toArrayList(): ArrayList<Element> {
    val list = ArrayList<Element>(length)
    for (i in 0 until length) {
        val item = item(i)
        if (item is Element) {
            list.add(item)
        }
    }
    return list
}


fun Element.getFirstElementByXPath(xpathStr: String): Element? {
    try {
        val xpathFactory = XPathFactory.newInstance()
        val xpath = xpathFactory.newXPath()
        val result = xpath.evaluate(xpathStr, this, XPathConstants.NODESET) as NodeList

        for (i in 0 until result.length) {
            val node = result.item(i)
            if (node is Element) {
                return node
            }
        }
    } catch (e: XPathExpressionException) {
        // Handle XPath expression exception
    }
    return null
}

fun Element.getElementsByXPath(xpathStr: String): NodeList? {
    try {
        val xpathFactory = XPathFactory.newInstance()
        val xpath = xpathFactory.newXPath()
        return xpath.evaluate(xpathStr, this, XPathConstants.NODESET) as NodeList
    } catch (e: XPathExpressionException) {
        // Handle XPath expression exception
    }
    return null
}

fun NodeList.forEachIndex(action: (element: Element, index: Int) -> Unit) {
    for (i in 0 until length) {
        val node = item(i)
        if (node is Element) {
            action(node, i)
        }
    }
}

object XMLParser {
    private val builder = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder()

    fun parse(xmlStr: String): Document = builder.parse(InputSource(StringReader(xmlStr)))
}