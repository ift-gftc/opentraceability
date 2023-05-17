interface IEventKDE {
    fun RegisterKDE(String ns, String name): Void
    fun InitializeKDE(String ns, String name): IEventKDE
    fun get_Namespace(): String
    fun set_Namespace(String value): Void
    fun get_Name(): String
    fun set_Name(String value): Void
    fun get_ValueType(): Type
    fun SetFromJson(JToken json): Void
    fun GetJson(): JToken
    fun SetFromXml(XElement xml): Void
    fun GetXml(): XElement
}
