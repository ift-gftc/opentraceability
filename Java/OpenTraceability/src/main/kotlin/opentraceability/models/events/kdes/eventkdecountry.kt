package opentraceability.models.events.kdes

import opentraceability.interfaces.IEventKDE
import opentraceability.utility.*
import org.json.*
import org.w3c.dom.Element
import java.lang.reflect.Type
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.reflect.KType
import kotlin.reflect.full.createType

class EventKDECountry: EventKDEBase, IEventKDE {

    override var valueType: KType = Country::class.createType()

    var value: Country? = null

    constructor(){}

    constructor(ns: String, name: String) {
        this.namespace = ns;
        this.name = name;
    }


    override fun getJson(): JSONObject? {
        TODO("Not yet implemented")
    }

    override fun getXml(): Element? {
        val value = this.value ?: return null
        // you would typically use a XML parser here
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
        val element = document.createElement(this.name)
        element.textContent = value.iso.toString()
        return element
    }

    override fun setFromJson(json: JSONObject) {
        val strValue = json.toString()
        this.value = Countries.parse(strValue)
    }

    override fun setFromXml(xml: Element) {
        this.value = Countries.parse(xml.textContent)
    }

    override fun toString(): String {
        if (value != null) {
            return value.toString()
        } else {
            return ""
        }
    }
}
