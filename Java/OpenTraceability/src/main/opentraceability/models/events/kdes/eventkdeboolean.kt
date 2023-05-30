package models.events.kdes

import interfaces.IEventKDE
import org.json.simple.JSONObject
import org.w3c.dom.Element
import java.lang.reflect.Type
import javax.xml.parsers.DocumentBuilderFactory

class EventKDEBoolean() : EventKDEBase(), IEventKDE {

    override var ValueType: Type = Boolean::class.java


    var value: Boolean? = null

    constructor(ns: String, name: String) : this() {
        this.Namespace = ns
        this.Name = name
    }

    override fun GetJson(): Any? {
        value?.let { return JSONObject().put("value", it) }
        return null
    }

    override fun GetXml(): Element? {
        value?.let {
            val documentBuilderFactory = DocumentBuilderFactory.newInstance()
            val documentBuilder = documentBuilderFactory.newDocumentBuilder()
            val document = documentBuilder.newDocument()
            val element = document.createElementNS(Namespace, Name)
            element.setAttributeNS(Constants.XSI_NAMESPACE, "type", "boolean")
            element.textContent = it.toString()
            return element
        }
        return null
    }

    override fun SetFromJson(json: JSONObject) {
        this.value = json["value"] as Boolean
    }

    override fun SetFromXml(xml: Element) {
        this.value = xml.textContent.toBoolean()
    }

    override fun toString(): String {
        return value?.toString() ?: ""
    }

}
