package opentraceability.mappers.epcis;

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

class utils {

    companion object {
        fun parseXml(xmlString: String): Document {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val inputStream = ByteArrayInputStream(xmlString.toByteArray())
            return builder.parse(inputStream)
        }
    }

}
