package models.masterdata.kdes

import com.fasterxml.jackson.core.JsonToken
import interfaces.IEventKDE
import interfaces.IMasterDataKDE
import models.events.kdes.EventKDEBase
import java.lang.reflect.Type
import javax.xml.bind.annotation.XmlElement

class MasterDataKDEObject : MasterDataKDEBase, IMasterDataKDE {
    private var _xml: XElement? = null
    private var _json: JToken? = null

    override val valueType: Class<*>
        get() = Any::class.java

    override val value: Any?
        get() = _xml ?: _json

    constructor() {
        // Default constructor
    }

    constructor(ns: String, name: String) {
        namespace = ns
        this.name = name
    }

    override fun setFromGS1WebVocabJson(json: JToken) {
        _xml = null
        _json = json
    }

    override fun getGS1WebVocabJson(): JToken? {
        return if (_xml != null) {
            val j = JsonConvert.serializeXNode(_xml)
            j
        } else _json
    }

    override fun setFromEPCISXml(xml: XElement) {
        _xml = xml
        _json = null
    }

    override fun getEPCISXml(): XElement? {
        return if (_xml != null) {
            _xml
        } else if (_json != null) {
            val xmlStr = (JsonConvert.deserializeXmlNode(_json.toString()) as XmlDocument).outerXml
            if (!xmlStr.isNullOrEmpty()) {
                XElement.parse(xmlStr)
            } else {
                null
            }
        } else {
            null
        }
    }
}
