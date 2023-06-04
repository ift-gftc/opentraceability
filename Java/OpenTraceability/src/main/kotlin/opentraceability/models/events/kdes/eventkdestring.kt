package opentraceability.models.events.kdes

import opentraceability.interfaces.IEventKDE
import org.json.JSONObject
import java.lang.reflect.Type
import org.w3c.dom.*
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.reflect.KType
import kotlin.reflect.full.createType

class EventKDEString: EventKDEBase, IEventKDE {

    override var valueType: KType = String::class.createType()

    var value: String? = null


    var type: String? = null
    var attributes: MutableMap<String, String> = mutableMapOf()


    constructor(ns: String, name: String) {
        this.namespace = ns;
        this.name = name;
    }


    override fun getJson(): JSONObject? {
        if (this.value.isNullOrBlank()) {
            return null
        } else {
            val json = JSONObject()
            json.put("keyName", this.value)
            return json
        }
    }

    override fun getXml(): Element? {
        if (this.value.isNullOrBlank()) {
            return null
        } else {
            val docFactory = DocumentBuilderFactory.newInstance()
            val docBuilder = docFactory.newDocumentBuilder()
            val document: Document = docBuilder.newDocument()

            val element = document.createElementNS(this.namespace, this.name)
            element.textContent = this.value

            // set the xsi type...
            for ((key, value) in this.attributes) {
                val attr = document.createAttributeNS(this.namespace, key)
                attr.value = value
                element.setAttributeNodeNS(attr)
            }

            return element
        }
    }

    override fun setFromJson(json: JSONObject) {
        this.value = json.toString()
    }

    override fun setFromXml(xml: Element) {
        this.value = xml.textContent

        for (i in 0 until xml.attributes.length) {
            val attr = xml.attributes.item(i) as Attr
            attributes.put( attr.nodeName, attr.nodeValue)
        }
    }

    override fun toString(): String {
        return value ?: ""
    }

}
