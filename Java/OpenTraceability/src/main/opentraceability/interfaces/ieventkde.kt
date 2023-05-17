import java.lang.reflect.Type
interface IEventKDE {
    fun RegisterKDE(ns: String, name: String): Void
    fun InitializeKDE(ns: String, name: String): IEventKDE
    fun get_Namespace(): String
    fun set_Namespace(value: String): Void
    fun get_Name(): String
    fun set_Name(value: String): Void
    fun get_ValueType(): Type
    fun SetFromJson(json: JToken): Void
    fun GetJson(): JToken
    fun SetFromXml(xml: XElement): Void
    fun GetXml(): XElement
}
