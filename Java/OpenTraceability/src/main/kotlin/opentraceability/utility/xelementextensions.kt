package utility


import java.util.*
import java.net.URI
import java.time.OffsetDateTime

class XElementExtensions {
    companion object {
        fun <String> GetDocumentNamespaces(x: XmlDocument): MutableMap<String, String> {
            if (x.rootTag == null) throw Exception("Root on XDocument is null.")

            val result = mutableMapOf<String, String>()
            val attributes = x.documentElement.attributes

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

        fun QueryXPath(x: Element, xpath: String): Element? {
            val xpathParts = xpath.splitXPath()
            val xfind = x.getElement(xpathParts[0])

            if (xfind == null) {
                return xfind
            } else if (xpathParts.size > 1) {
                return xfind.queryXPath(xpathParts.drop(1).joinToString("/"))
            } else {
                return xfind
            }
        }

        fun QueryJPath(j: JSONObject, jpath: String): JSONObject? {
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

        fun attributeISODateTime(x: Element,attName: String): OffsetDateTime? {
            val strValue = x.getAttribute(attName)?.value

            if (!strValue.isNullOrEmpty()) {
                return strValue.tryConvertToDateTimeOffset()
            }

            return null
        }



        fun attributeURI(x: Element,attName: String): URI? {
            val strValue = x.getAttribute(attName)?.value

            if (!strValue.isNullOrEmpty()) {
                try {
                    return URI(strValue)
                } catch (ex: Exception) {
                    val exception = Exception("Failed to create URI from string = $strValue", ex)
                    OTLogger.error(exception)
                    throw exception
                }
            }

            return null
        }

        fun attributeBoolean(x: Element,attName: String): Boolean? {
            val strValue = x.getAttribute(attName)?.value

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

        fun attributeDouble(x: Element,attName: String): Double? {
            val strValue = x.getAttribute(attName)?.value

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

        fun attributeUOM(x: Element,attName: String): UOM? {
            val strValue = x.getAttribute(attName)?.value

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

        fun addStringElement(x: Element,xname: XName, value: String?) {
            if (!value.isNullOrBlank()) {
                x.add(XmlElement(xname, value))
            }
        }

        fun addDateTimeOffsetISOElement(x: Element,xname: XName, value: OffsetDateTime?) {
            if (value != null) {
                x.add(XmlElement(xname, value.toString("o")))
            }
        }

    }
}
