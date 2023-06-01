import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpressionException
import javax.xml.xpath.XPathFactory

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