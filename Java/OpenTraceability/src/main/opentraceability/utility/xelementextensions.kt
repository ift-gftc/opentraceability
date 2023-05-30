package utility
import com.fasterxml.jackson.core.JsonToken
import com.intellij.psi.xml.XmlDocument
import javax.xml.bind.annotation.*
import java.util.*
import java.net.URI
import java.time.OffsetDateTime
class XElementExtensions {
    companion object {
        fun <String> GetDocumentNamespaces(x: XmlDocument): MutableMap<String, String> {
            if (this.documentElement == null) throw Exception("Root on XDocument is null.")

            val result = mutableMapOf<String, String>()
            val attributes = this.documentElement.attributes

            for (i in 0 until attributes.length) {
                val attribute = attributes.item(i) as? Attr

                if (attribute?.isNamespaceDeclaration == true) {
                    val namespaceKey = if (attribute.name.namespaceURI == null) "" else attribute.localName
                    val namespaceValue = attribute.value

                    result[namespaceKey] = namespaceValue
                }
            }

            return result
        }

        fun QueryXPath(x: XmlElement, xpath: String): XmlElement? {
            val xpathParts = xpath.splitXPath()
            val xfind = this.getElement(xpathParts[0])

            if (xfind == null) {
                return xfind
            } else if (xpathParts.size > 1) {
                return xfind.queryXPath(xpathParts.drop(1).joinToString("/"))
            } else {
                return xfind
            }
        }

        fun QueryJPath(j: JsonToken, jpath: String): JsonToken? {
            val jpathParts = jpath.split('.').toList()
            val jfind = this[jpathParts[0]]

            if (jfind == null) {
                return jfind
            } else if (jpathParts.size > 1) {
                return jfind.queryJPath(jpathParts.drop(1).joinToString("/"))
            } else {
                return jfind
            }
        }

        fun attributeISODateTime(attName: String): DateTimeOffset? {
            val strValue = this.getAttribute(attName)?.value

            if (!strValue.isNullOrEmpty()) {
                return strValue.tryConvertToDateTimeOffset()
            }

            return null
        }

        fun attributeURI(attName: String): Uri? {
            val strValue = this.getAttribute(attName)?.value

            if (!strValue.isNullOrEmpty()) {
                try {
                    return Uri(strValue)
                } catch (ex: Exception) {
                    val exception = Exception("Failed to create URI from string = $strValue", ex)
                    OTLogger.error(exception)
                    throw exception
                }
            }

            return null
        }

        fun attributeBoolean(attName: String): Boolean? {
            val strValue = this.getAttribute(attName)?.value

            if (!strValue.isNullOrEmpty()) {
                try {
                    return strValue.toBoolean()
                } catch (ex: Exception) {
                    val exception = Exception("Failed to create Boolean from string = $strValue", ex)
                    OTLogger.error(exception)
                    throw exception
                }
            }

            return null
        }

        fun attributeDouble(attName: String): Double? {
            val strValue = this.getAttribute(attName)?.value

            if (!strValue.isNullOrEmpty()) {
                try {
                    return strValue.toDouble()
                } catch (ex: Exception) {
                    val exception = Exception("Failed to create Double from string = $strValue", ex)
                    OTLogger.error(exception)
                    throw exception
                }
            }

            return null
        }

        fun attributeUOM(attName: String): UOM? {
            val strValue = this.getAttribute(attName)?.value

            if (!strValue.isNullOrEmpty()) {
                try {
                    return UOM.parseFromName(strValue)
                } catch (ex: Exception) {
                    val exception = Exception("Failed to create UOM from string = $strValue", ex)
                    OTLogger.error(exception)
                    throw exception
                }
            }

            return null
        }

        fun addStringElement(xname: XName, value: String?) {
            if (!value.isNullOrBlank()) {
                this.add(XElement(xname, value))
            }
        }

        fun addDateTimeOffsetISOElement(xname: XName, value: DateTimeOffset?) {
            if (value != null) {
                this.add(XElement(xname, value.toString("o")))
            }
        }

    }
}
