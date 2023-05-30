package models.masterdata.kdes

import com.fasterxml.jackson.core.JsonToken
import interfaces.IEventKDE
import interfaces.IMasterDataKDE
import models.events.kdes.EventKDEBase
import java.lang.reflect.Type
import javax.xml.bind.annotation.XmlElement


class MasterDataKDEString : MasterDataKDEBase, IMasterDataKDE {
    override val valueType: Class<*>
        get() = String::class.java

    var value: String? = null
    var type: String? = null
    val attributes: MutableMap<String, String> = mutableMapOf()

    constructor() {
        // Default constructor
    }

    constructor(ns: String, name: String) {
        this.Namespace = ns
        this.Name = name
    }

    fun getJson(): JToken? {
        return if (value.isNullOrBlank()) {
            null
        } else {
            JToken.fromObject(value)
        }
    }

    fun getXml(): XElement? {
        return if (value.isNullOrBlank()) {
            null
        } else {
            val xname = (XNamespace)namespace + this.Name
            val x = XElement(xname, value)

            // set the xsi type...
            attributes.forEach { (key, value) ->
                x.add(XAttribute(key, value))
            }

            x
        }
    }

    fun setFromJson(json: JToken) {
        value = json.toString()
    }

    fun setFromXml(xml: XElement) {
        value = xml.value

        xml.attributes().forEach { xatt ->
            attributes[xatt.name.toString()] = xatt.value
        }
    }

    override fun toString(): String {
        return value ?: ""
    }

    override fun setFromGS1WebVocabJson(json: JToken) {
        value = json.toString()
    }

    override fun getGS1WebVocabJson(): JToken? {
        return if (value != null) {
            JToken.fromObject(value)
        } else {
            null
        }
    }

    override fun setFromEPCISXml(xml: XElement) {
        name = xml.attribute("id")?.value ?: ""
        value = xml.value
    }

    override fun getEPCISXml(): XElement? {
        return if (value != null) {
            val x = XElement("attribute")
            x.add(XAttribute("id", this.Name))
            x.value = value
            x
        } else {
            null
        }
    }
}
