package models.events.kdes

import interfaces.IEventKDE
import java.lang.reflect.Type
import org.w3c.dom.*
import javax.xml.parsers.DocumentBuilderFactory
import java.io.StringReader
import org.json.*

class EventKDEObject: EventKDEBase, IEventKDE {

    override var ValueType: Type = Object::class.java

    var Value: Object? = null


    var _xml: Element? = null
    var _json: JSONObject? = null


    constructor(ns: String, name: String) {
        this.Namespace = ns;
        this.Name = name;
    }


    override fun getJson(): JSONObject? {
        if (_xml != null) {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val xmlDocument: Document = builder.parse(_xml.createReader())
            val j = XML.toString(xmlDocument)
            return JSONObject(j)
        } else if (_json != null) {
            return _json
        } else {
            return null
        }
    }








    override fun getXml(): Element? {
        if (_xml != null) {
            val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val doc: Document = docBuilder.parse(StringReader(_xml))
            return doc.documentElement
        } else if (_json != null) {
            val xmlString = XML.toString(_json)
            if (xmlString.isNotEmpty()) {
                val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                val doc: Document = docBuilder.parse(StringReader(xmlString))
                return doc.createElementNS(this.Namespace, this.Name).also {
                    it.appendChild(doc.importNode(doc.documentElement, true))
                }
            } else {
                return null
            }
        } else {
            return null
        }
    }

    override fun setFromJson(json: JSONObject) {
        _xml = null
        _json = json
    }

    override fun setFromXml(xml: Element) {
        _xml = xml
        _json = null
    }

    override fun toString(): String {
        if (Value != null) {
            return Value.toString()
        } else {
            return ""
        }
    }
}
