package opentraceability.models.events.kdes

import opentraceability.interfaces.IEventKDE
import opentraceability.Constants
import org.json.JSONObject
import java.lang.reflect.Type
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.*
import kotlin.reflect.KType
import kotlin.reflect.full.createType

class EventKDEDouble: EventKDEBase, IEventKDE {

    override var valueType: KType = Double::class.createType()

    var value: Double? = null

    constructor(){}

    constructor(ns: String, name: String) {
        this.namespace = ns;
        this.name = name;
    }


    override fun getJson(): Any? {
        if (this.value == null) {
            return null
        } else {
            val json = JSONObject()
            json.put("keyName", this.value)
            return json
        }
    }

    override fun getXml(): Element? {
        val value = this.value ?: return null

        val docFactory = DocumentBuilderFactory.newInstance()
        val docBuilder = docFactory.newDocumentBuilder()
        val document: Document = docBuilder.newDocument()

        val element = document.createElementNS(this.namespace, this.name)
        element.textContent = value.toString()

        val xsiTypeAttr = document.createAttributeNS(Constants.XSI_NAMESPACE, "type")
        xsiTypeAttr.value = "number"
        element.setAttributeNodeNS(xsiTypeAttr)

        return element
    }

    override fun setFromJson(json: JSONObject) {
        this.value = json["value"] as Double
    }

    override fun setFromXml(xml: Element) {
        this.value = xml.textContent.toDoubleOrNull()
    }

    override fun toString(): String {
        if (value != null) {
            return value.toString()
        } else {
            return ""
        }
    }
}
