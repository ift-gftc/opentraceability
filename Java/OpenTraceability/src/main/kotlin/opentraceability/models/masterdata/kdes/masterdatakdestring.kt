package models.masterdata.kdes

import interfaces.IMasterDataKDE
import org.json.JSONObject
import org.w3c.dom.Document
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

class MasterDataKDEString : MasterDataKDEBase, IMasterDataKDE {

    override var ValueType: Class<*>? = String::class.java

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
            val xname = "$Namespace$Name"
            val document: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
            val element: Element = document.createElement(xname)
            element.textContent = value

            // Set the xsi type...
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
        value = xml.textContent

        for (i in 0 until xml.attributes.length) {
            val attribute = xml.attributes.item(i)
            attributes[attribute.nodeName] = attribute.nodeValue
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
        Name = xml.getAttribute("id") ?: ""
        value = xml.textContent
    }



    override  fun getEPCISXml(): Element? {
        return if (value != null) {
            val document: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
            val element: Element = document.createElement("attribute")
            element.setAttribute("id", Name)
            element.textContent = value
            element
        } else {
            null
        }
    }


}
