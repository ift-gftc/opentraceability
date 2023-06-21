package opentraceability.models.events.kdes

import opentraceability.interfaces.IEventKDE
import opentraceability.Constants
import org.json.JSONObject
import org.w3c.dom.Element
import java.lang.reflect.Type
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType

class EventKDEBoolean() : EventKDEBase(), IEventKDE {

    override var valueType: KType = Boolean::class.starProjectedType

    var value: Boolean? = null


    constructor(ns: String, name: String) : this() {
        this.namespace = ns
        this.name = name
    }

    override fun getJson(): Any? {
        value?.let { return JSONObject().put("value", it) }
        return null
    }

    override fun getXml(): Element? {
        value?.let {
            val documentBuilderFactory = DocumentBuilderFactory.newInstance()
            val documentBuilder = documentBuilderFactory.newDocumentBuilder()
            val document = documentBuilder.newDocument()
            val element = document.createElementNS(namespace, name)
            element.setAttributeNS(Constants.XSI_NAMESPACE, "type", "boolean")
            element.textContent = it.toString()
            return element
        }
        return null
    }

    override fun setFromJson(json: JSONObject) {
        this.value = json["value"] as Boolean
    }

    override fun setFromXml(xml: Element) {
        this.value = xml.textContent.toBoolean()
    }

    override fun toString(): String {
        return value?.toString() ?: ""
    }

}
