package opentraceability.models.masterdata.kdes

import opentraceability.interfaces.IMasterDataKDE
import org.json.JSONObject
import org.w3c.dom.Document
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class MasterDataKDEString : MasterDataKDEBase, IMasterDataKDE {

    override var valueType: KType = typeOf<String>()

    var value: String? = null
    var type: String? = null
    val attributes: MutableMap<String, String> = mutableMapOf()

    constructor(){}

    constructor(ValueType: KType) {
        // Default constructor
        this.valueType = ValueType
    }

    constructor(ns: String, name: String) {
        super.namespace = ns
        super.name = name
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
        name = xml.getAttribute("id") ?: ""
        value = xml.textContent
    }



    override  fun getEPCISXml(): Element? {
        return if (value != null) {
            val document: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
            val element: Element = document.createElement("attribute")
            element.setAttribute("id", name)
            element.textContent = value
            element
        } else {
            null
        }
    }


}
