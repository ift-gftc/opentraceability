package interfaces


import org.w3c.dom.Element
import org.json.JSONObject

interface IMasterDataKDE {

    var Namespace: String
    var Name: String
    var ValueType: Class<*>

    fun SetFromGS1WebVocabJson(json: JSONObject)
    fun GetGS1WebVocabJson(): JSONObject?
    fun SetFromEPCISXml(xml: Element)
    fun GetEPCISXml(): Element?
}
