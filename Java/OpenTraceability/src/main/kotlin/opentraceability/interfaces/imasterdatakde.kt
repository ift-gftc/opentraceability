package interfaces


import org.jdom2.Element
import org.json.JSONObject

interface IMasterDataKDE {

    var namespace: String
    var name: String
    var valueType: Class<*>

    fun setFromGS1WebVocabJson(json: JSONObject)
    fun getGS1WebVocabJson(): JSONObject?
    fun setFromEPCISXml(xml: Element)
    fun getEPCISXml(): Element?
}
