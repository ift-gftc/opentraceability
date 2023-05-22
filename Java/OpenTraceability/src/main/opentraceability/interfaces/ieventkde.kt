package interfaces
import com.fasterxml.jackson.core.JsonToken
import javax.xml.bind.annotation.*
import java.lang.reflect.Type
interface IEventKDE {
    fun RegisterKDE(ns: String, name: String): Void
    fun InitializeKDE(ns: String, name: String): IEventKDE
    fun get_Namespace(): String
    fun set_Namespace(value: String): Void
    fun get_Name(): String
    fun set_Name(value: String): Void
    fun get_ValueType(): Type
    fun SetFromJson(json: JsonToken): Void
    fun GetJson(): JsonToken
    fun SetFromXml(xml: XmlElement): Void
    fun GetXml(): XmlElement
}
