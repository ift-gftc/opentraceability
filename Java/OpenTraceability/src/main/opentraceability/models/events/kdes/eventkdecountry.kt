package models.events.kdes

import com.fasterxml.jackson.core.JsonToken
import interfaces.IEventKDE
import org.json.simple.JSONObject
import org.w3c.dom.Element
import utility.Countries
import utility.Country
import java.lang.reflect.Type
import javax.xml.parsers.DocumentBuilderFactory

class EventKDECountry: EventKDEBase, IEventKDE {

    override var ValueType: Type = Country::class.java

    var Value: Country? = null


    constructor(ns: String, name: String) {
        this.Namespace = ns;
        this.Name = name;
    }


    override fun GetJson(): JsonToken? {
        TODO("Not yet implemented")
    }

    override fun GetXml(): Element? {
        val value = this.Value ?: return null
        // you would typically use a XML parser here
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
        val element = document.createElement(this.Name)
        element.textContent = value.ISO.toString()
        return element
    }

    override fun SetFromJson(json: JSONObject) {
        val strValue = json.toString()
        this.Value = Countries.Parse(strValue)
    }

    override fun SetFromXml(xml: Element) {
        this.Value = Countries.Parse(xml.textContent)
    }

    override fun toString(): String {
        if (Value != null) {
            return Value.toString()
        } else {
            return ""
        }
    }
}
