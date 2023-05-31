package models.events.kdes

import interfaces.IEventKDE
import org.json.JSONObject
import java.lang.reflect.Type
import org.w3c.dom.*
import javax.xml.parsers.DocumentBuilderFactory

class EventKDEString: EventKDEBase, IEventKDE {

    override var ValueType: Type = String::class.java

    var Value: String? = null


    var Type: String? = null
    var Attributes: MutableMap<String, String> = mutableMapOf()


    constructor(ns: String, name: String) {
        this.Namespace = ns;
        this.Name = name;
    }


    override fun getJson(): JSONObject? {
        if (this.Value.isNullOrBlank()) {
            return null
        } else {
            val json = JSONObject()
            json.put("keyName", this.Value)
            return json
        }
    }

    override fun getXml(): Element? {
        if (this.Value.isNullOrBlank()) {
            return null
        } else {
            val docFactory = DocumentBuilderFactory.newInstance()
            val docBuilder = docFactory.newDocumentBuilder()
            val document: Document = docBuilder.newDocument()

            val element = document.createElementNS(this.Namespace, this.Name)
            element.textContent = this.Value

            // set the xsi type...
            for ((key, value) in this.Attributes) {
                val attr = document.createAttributeNS(this.Namespace, key)
                attr.value = value
                element.setAttributeNodeNS(attr)
            }

            return element
        }
    }

    override fun setFromJson(json: JSONObject) {
        this.Value = json.toString()
    }

    override fun setFromXml(xml: Element) {
        this.Value = xml.value

        for (xatt in xml.attributes()) {
            Attributes[xatt.name.toString()] = xatt.value
        }
    }

    override fun toString(): String {
        return Value ?: ""
    }

}
