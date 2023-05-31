package models.events.kdes

import interfaces.IEventKDE
import org.json.JSONObject
import java.lang.reflect.Type
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.*

class EventKDEDouble: EventKDEBase, IEventKDE {

    override var ValueType: Type = Double::class.java

    var Value: Double? = null

    constructor() {

    }

    constructor(ns: String, name: String) {
        this.Namespace = ns;
        this.Name = name;
    }


    override fun getJson(): Any? {
        if (this.Value == null) {
            return null
        } else {
            val json = JSONObject()
            json.put("keyName", this.Value)
            return json
        }
    }

    override fun getXml(): Element? {
        val value = this.Value ?: return null

        val docFactory = DocumentBuilderFactory.newInstance()
        val docBuilder = docFactory.newDocumentBuilder()
        val document: Document = docBuilder.newDocument()

        val element = document.createElementNS(this.Namespace, this.Name)
        element.textContent = value.toString()

        val xsiTypeAttr = document.createAttributeNS(Constants.XSI_NAMESPACE, "type")
        xsiTypeAttr.value = "number"
        element.setAttributeNodeNS(xsiTypeAttr)

        return element
    }

    override fun setFromJson(json: JSONObject) {
        this.Value = json["value"] as Double
    }

    override fun setFromXml(xml: Element) {
        this.Value = xml.textContent.toDoubleOrNull()
    }

    override fun toString(): String {
        if (Value != null) {
            return Value.toString()
        } else {
            return ""
        }
    }
}
