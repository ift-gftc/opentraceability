import org.json.JSONObject
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpressionException
import javax.xml.xpath.XPathFactory
import interfaces.IMasterDataKDE
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.json.XML
import org.xml.sax.InputSource
import toXmlString
import java.io.StringReader

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


fun JSONObject.toXmlString(): String {
    val xmlOptions = XMLSerializer.XmlOptions()
    xmlOptions.setIndenting(true)
    xmlOptions.setCharacterEncodingScheme("UTF-8")
    xmlOptions.setUseDoubleQuotes(true)
    return XMLSerializer.toXmlString(this, xmlOptions)
}

object XMLParser {
    private val builder = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder()

    fun parse(xmlStr: String): Document = builder.parse(InputSource(StringReader(xmlStr)))
}