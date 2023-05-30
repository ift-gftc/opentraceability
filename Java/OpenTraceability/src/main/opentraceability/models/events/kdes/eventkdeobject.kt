package models.events.kdes

import interfaces.IEventKDE
import org.json.simple.JSONObject
import java.lang.reflect.Type
import org.w3c.dom.Element
import org.w3c.dom.Document
import javax.xml.parsers.DocumentBuilderFactory
import java.io.StringReader
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import java.io.StringWriter


class EventKDEObject: EventKDEBase, IEventKDE {

    override var ValueType: Type = Object::class.java

    var Value: Object? = null


    var _xml: Element? = null
    var _json: JSONObject? = null


    constructor(ns: String, name: String) {
        this.Namespace = ns;
        this.Name = name;
    }


    override fun GetJson(): JSONObject? {
        if (_xml != null) {
            // Convert _xml to JObject
            val docFactory = DocumentBuilderFactory.newInstance()
            val docBuilder = docFactory.newDocumentBuilder()
            val document: Document = docBuilder.parse(StringReader(_xml))

            val transformerFactory = TransformerFactory.newInstance()
            val transformer = transformerFactory.newTransformer()
            val source = DOMSource(document)
            val writer = StringWriter()
            val result = StreamResult(writer)
            transformer.transform(source, result)

            val xmlString = writer.toString()
            val json = XML.toJSONObject(xmlString)

            return JSONObject(json.toString(4)) // 4 is for indentation
        } else if (_json != null) {
            return _json
        } else {
            return null
        }
    }

    override fun GetXml(): Element? {
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

    override fun SetFromJson(json: JSONObject) {
        _xml = null
        _json = json
    }

    override fun SetFromXml(xml: Element) {
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
