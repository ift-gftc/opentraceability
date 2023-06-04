package opentraceability.models.masterdata.kdes

import opentraceability.interfaces.IMasterDataKDE
import org.json.JSONObject
import org.w3c.dom.Element
import org.json.XML
import opentraceability.utility.*
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class MasterDataKDEObject : MasterDataKDEBase, IMasterDataKDE {
    private var _xml: Element? = null
    private var _json: JSONObject? = null

    override var valueType: KType = typeOf<Object>()

    var value: Any? = null
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
            val json = _xml!!.toJSON()
            return json
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
            return _json!!.toXML()
        } else {
            null
        }
    }
}
