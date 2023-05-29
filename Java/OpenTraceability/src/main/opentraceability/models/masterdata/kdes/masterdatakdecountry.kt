package opentraceability.models.masterdata.kdes

import com.fasterxml.jackson.core.JsonToken
import opentraceability.interfaces.IEventKDE
import opentraceability.interfaces.IMasterDataKDE
import opentraceability.models.events.kdes.EventKDEBase
import opentraceability.utility.Country
import java.lang.reflect.Type
import javax.xml.bind.annotation.XmlElement

class MasterDataKDECountry /*: MasterDataKDEBase, IMasterDataKDE*/ {


    var Value: Country? = null

    var ValueType: Type = Country::class.java

    fun GetEPCISXml(): XmlElement? {
        TODO("Not yet implemented")
    }
    fun GetGS1WebVocabJson(): JsonToken? {
        TODO("Not yet implemented")
    }
    fun GetXml(): XmlElement? {
        TODO("Not yet implemented")
    }

    fun SetFromEPCISXml(xml:XmlElement) {
        TODO("Not yet implemented")
    }

    fun SetFromGS1WebVocabJson(json:JsonToken) {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        if (Value != null) {
            return Value.toString()
        } else {
            return ""
        }
    }
}
