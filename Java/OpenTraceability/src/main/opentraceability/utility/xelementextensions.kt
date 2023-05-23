package utility
import com.fasterxml.jackson.core.JsonToken
import com.intellij.psi.xml.XmlDocument
import javax.xml.bind.annotation.*
import java.util.*
import java.net.URI
import java.time.OffsetDateTime
class XElementExtensions {
    companion object{
    }


    fun<String> GetDocumentNamespaces(x: XmlDocument): MutableMap<String, String> {
        TODO("Not yet implemented")
    }


    fun QueryXPath(x: XmlElement, xpath: String): XmlElement {
        TODO("Not yet implemented")
    }


    fun QueryJPath(j: JsonToken, jpath: String): JsonToken {
        TODO("Not yet implemented")
    }


    fun AttributeISODateTime(x: XmlElement, attName: String): OffsetDateTime? {
        TODO("Not yet implemented")
    }


    fun AttributeURI(x: XmlElement, attName: String): URI? {
        TODO("Not yet implemented")
    }


    fun AttributeBoolean(x: XmlElement, attName: String): Boolean? {
        TODO("Not yet implemented")
    }

    fun AttributeDouble(x: XmlElement, attName: String): Double? {
        TODO("Not yet implemented")
    }

    fun AttributeUOM(x: XmlElement, attName: String): UOM {
        TODO("Not yet implemented")
    }

    fun AddStringElement(x: XmlElement, xname: XName, value: String) {
        TODO("Not yet implemented")
    }

    fun AddDateTimeOffsetISOElement(x: XmlElement, xname: XName, value: OffsetDateTime?) {
        TODO("Not yet implemented")
    }
}
