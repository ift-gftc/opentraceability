package models.masterdata.kdes

import interfaces.IMasterDataKDE
import org.json.JSONObject
import org.w3c.dom.Element
import org.json.XML
import toXmlString

class MasterDataKDEObject : MasterDataKDEBase, IMasterDataKDE {
    private var _xml: Element? = null
    private var _json: JSONObject? = null

    override var ValueType: Class<*>? = null
        get() = Any::class.java

    var Value: Any? = null
        get() = _xml ?: _json

    constructor() {
        // Default constructor
    }

    constructor(ns: String, name: String) {
        super.Namespace = ns
        super.Name = name
    }

    override fun setFromGS1WebVocabJson(json: JSONObject) {
        _xml = null
        _json = json
    }

    override fun getGS1WebVocabJson(): JSONObject? {
        return if (_xml != null) {
            val jsonString = org.json.XML.toJSONObject(_xml.toString()).toString()
            JSONObject(jsonString)
        } else {
            _json
        }
    }


    override fun setFromEPCISXml(xml: Element) {
        _xml = xml
        _json = null
    }



    override fun getEPCISXml(): Element? {
        return if (_xml != null) {
            _xml
        } else if (_json != null) {
            val xmlDoc = XML.toJSONObject(_json.toString()).let { json ->
                val xmlStr = json.toXmlString()
                val doc = XMLParser.parse(xmlStr)
                doc.documentElement
            }
            xmlDoc as? Element
        } else {
            null
        }
    }
}
