package interfaces

import com.fasterxml.jackson.core.JsonToken
import javax.xml.bind.annotation.*
import java.lang.reflect.Type

interface IMasterDataKDE {

    var Namespace: String
    var Name: String
    var ValueType: Type

    fun SetFromGS1WebVocabJson(json: JsonToken)
    fun GetGS1WebVocabJson(): JsonToken?
    fun SetFromEPCISXml(xml: XmlElement)
    fun GetEPCISXml(): XmlElement?
}
