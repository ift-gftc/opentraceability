package interfaces
import com.fasterxml.jackson.core.JsonToken
import javax.xml.bind.annotation.*
import models.identifiers.*
import java.lang.reflect.Type
interface IMasterDataKDE {
    fun get_Namespace(): String
    fun set_Namespace(value: String): Void
    fun get_Name(): String
    fun set_Name(value: String): Void
    fun get_ValueType(): Type
    fun SetFromGS1WebVocabJson(json: JsonToken): Void
    fun GetGS1WebVocabJson(): JsonToken
    fun SetFromEPCISXml(xml: XmlElement): Void
    fun GetEPCISXml(): XmlElement
}
