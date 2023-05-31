package models.masterdata.kdes

import interfaces.IMasterDataKDE
import org.json.JSONObject
import org.w3c.dom.Element

class MasterDataKDEObject : MasterDataKDEBase, IMasterDataKDE {
    private var _xml: Element? = null
    private var _json: JSONObject? = null

    override val valueType: Class<*>
        get() = Any::class.java

    override val value: Any?
        get() = _xml ?: _json

    constructor() {
        // Default constructor
    }

    constructor(ns: String, name: String) {
        super.namespace = ns
        super.name = name
    }

    override fun setFromGS1WebVocabJson(json: JSONObject) {
        _xml = null
        _json = json
    }

    override fun getGS1WebVocabJson(): JSONObject? {
        return if (_xml != null) {
            val j = JsonConvert.serializeXNode(_xml)
            j
        } else _json
    }

    override fun setFromEPCISXml(xml: Element) {
        _xml = xml
        _json = null
    }

    override fun getEPCISXml(): Element? {
        return if (_xml != null) {
            _xml
        } else if (_json != null) {
            val xmlStr = (JsonConvert.deserializeXmlNode(_json.toString()) as XmlDocument).outerXml
            if (!xmlStr.isNullOrEmpty()) {
                Element.parse(xmlStr)
            } else {
                null
            }
        } else {
            null
        }
    }
}
