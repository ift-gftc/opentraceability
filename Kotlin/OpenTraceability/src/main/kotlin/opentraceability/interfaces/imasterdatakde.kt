package opentraceability.interfaces


import org.w3c.dom.Element
import org.json.JSONObject
import kotlin.reflect.KType

interface IMasterDataKDE {

    var namespace: String
    var name: String
    var valueType: KType

    fun setFromGS1WebVocabJson(json: JSONObject)
    fun getGS1WebVocabJson(): JSONObject?
    fun setFromEPCISXml(xml: Element)
    fun getEPCISXml(): Element?
}
