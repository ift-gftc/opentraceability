package interfaces

import com.fasterxml.jackson.core.JsonToken
import javax.xml.bind.annotation.*
import java.lang.reflect.Type

interface IMasterDataKDE {

    var namespace: String
    var name: String
    var valueType: Class<*>

    fun setFromGS1WebVocabJson(json: JsonToken)
    fun getGS1WebVocabJson(): JsonToken?
    fun setFromEPCISXml(xml: XmlElement)
    fun getEPCISXml(): XmlElement?
}
