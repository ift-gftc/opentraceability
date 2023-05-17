interface IMasterDataKDE {
    fun get_Namespace(): String
    fun set_Namespace(String value): Void
    fun get_Name(): String
    fun set_Name(String value): Void
    fun get_ValueType(): Type
    fun SetFromGS1WebVocabJson(JToken json): Void
    fun GetGS1WebVocabJson(): JToken
    fun SetFromEPCISXml(XElement xml): Void
    fun GetEPCISXml(): XElement
}
