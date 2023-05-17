import java.lang.reflect.Type
interface IMasterDataKDE {
    fun get_Namespace(): String
    fun set_Namespace(value: String): Void
    fun get_Name(): String
    fun set_Name(value: String): Void
    fun get_ValueType(): Type
    fun SetFromGS1WebVocabJson(json: JToken): Void
    fun GetGS1WebVocabJson(): JToken
    fun SetFromEPCISXml(xml: XElement): Void
    fun GetEPCISXml(): XElement
}
