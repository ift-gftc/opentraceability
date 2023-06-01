package interfaces


import org.w3c.dom.Element
import org.json.JSONObject

interface IMasterDataKDE {

    var Namespace: String
    var Name: String
    var ValueType: Class<*>?

    fun setFromGS1WebVocabJson(json: JSONObject)
    fun getGS1WebVocabJson(): JSONObject?
    fun setFromEPCISXml(xml: Element)
    fun getEPCISXml(): Element?
}
