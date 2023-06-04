package opentraceability.models.events.kdes

import opentraceability.interfaces.IEventKDE
import opentraceability.Constants
import java.lang.reflect.Type
import org.w3c.dom.*
import javax.xml.parsers.DocumentBuilderFactory
import java.io.StringReader
import org.json.*
import javax.xml.parsers.DocumentBuilder
import org.xml.sax.InputSource
import kotlin.reflect.KType
import kotlin.reflect.full.createType

class EventKDEObject: EventKDEBase, IEventKDE {

    override var valueType: KType = Object::class.createType()

    var value: Object? = null


    var _xml: Element? = null
    var _json: JSONObject? = null


    constructor(ns: String, name: String) {
        this.namespace = ns;
        this.name = name;
    }


    override fun getJson(): JSONObject? {
        if ( _xml != null   ){
            val factory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
            val builder: DocumentBuilder = factory.newDocumentBuilder()
            val xmlDoc: Document = builder.parse(InputSource(StringReader(_xml.toString())))

            val json = XML.toJSONObject(xmlDoc.toString())
            return JSONObject(json.toString())
        }
        else if (_json != null)
        {
            return _json;
        }
        else
        {
            return null;
        }
    }




    override fun getXml(): Element? {
        if (_xml != null) {
            return _xml
        } else if (_json != null) {

            val xmlDoc = XML.toJSONObject(_json.toString())
            val xmlStr = xmlDoc.toString()

            if (!xmlStr.isNullOrEmpty()) {
                val factory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
                val builder: DocumentBuilder = factory.newDocumentBuilder()
                val document: Document = builder.parse(InputSource(StringReader(xmlStr)))

                return document.documentElement
            }

            return null
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
        if (value != null) {
            return value.toString()
        } else {
            return ""
        }
    }
}
