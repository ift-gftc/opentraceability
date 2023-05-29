package opentraceability.models.masterdata.kdes

import com.fasterxml.jackson.core.JsonToken
import opentraceability.interfaces.IEventKDE
import opentraceability.models.events.kdes.EventKDEBase
import java.lang.reflect.Type
import javax.xml.bind.annotation.XmlElement

class MasterDataKDEObject /*: MasterDataKDEBase, IMasterDataKDE*/ {

    var ValueType: Type = Object::class.java

    var Value: Object? = null

    constructor() {

    }

    constructor(ns: String, name: String) {
        //this.Namespace = ns;
        //this.Name = name;
    }

    fun SetFromGS1WebVocabJson(json: JsonToken) {
        TODO("Not yet implemented")
    }

    fun GetGS1WebVocabJson(): JsonToken? {
        TODO("Not yet implemented")
    }

    fun SetFromEPCISXml(xml: XmlElement) {
        TODO("Not yet implemented")
    }


    fun GetEPCISXml(): XmlElement? {
        TODO("Not yet implemented")
    }
}
