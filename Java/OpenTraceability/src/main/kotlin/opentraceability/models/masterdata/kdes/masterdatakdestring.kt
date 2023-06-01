package models.masterdata.kdes

import interfaces.IMasterDataKDE
import org.json.JSONObject
import org.w3c.dom.Element

class MasterDataKDEString : MasterDataKDEBase, IMasterDataKDE {
    override var ValueType: Class<*>
        get() = String::class.java

    var value: String? = null
    var type: String? = null
    val attributes: MutableMap<String, String> = mutableMapOf()

    constructor(ValueType: Class<*>) {
        // Default constructor
        this.ValueType = ValueType
    }

    constructor(ns: String, name: String) {
        super.Namespace = ns
        super.Name = name
    }

    fun getJson(): JSONObject? {
        return if (value.isNullOrBlank()) {
            null
        } else {
            JSONObject(value)
        }
    }

    fun getXml(): Element? {
        return if (value.isNullOrBlank()) {
            null
        } else {
            val xname = "$namespace$name"
            val element = Element(xname).setText(value)

            // set the xsi type...
            attributes.forEach { (key, value) ->
                element.setAttribute(key, value)
            }

            element
        }
    }

    fun setFromJson(json: JSONObject) {
        value = json.toString()
    }

    fun setFromXml(xml: Element) {
        value = xml.text

        xml.attributes.forEach { xatt ->
            attributes[xatt.name] = xatt.value
        }
    }

    override fun toString(): String {
        return value ?: ""
    }

    override fun setFromGS1WebVocabJson(json: JSONObject) {
        value = json.toString()
    }

    override fun getGS1WebVocabJson(): JSONObject? {
        return if (value != null) {
            JSONObject(value)
        } else {
            null
        }
    }

    override fun setFromEPCISXml(xml: Element) {
        name = xml.getAttributeValue("id") ?: ""
        value = xml.value
    }

    override fun getEPCISXml(): Element? {
        return if (value != null) {
            val element = Element("attribute")
            element.setAttribute("id", name)
            element.text = value
            element
        } else {
            null
        }
    }
}
