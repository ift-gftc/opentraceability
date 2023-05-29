package opentraceability.models.masterdata.kdes

import com.fasterxml.jackson.core.JsonToken
import opentraceability.interfaces.IEventKDE
import opentraceability.models.events.kdes.EventKDEBase
import java.lang.reflect.Type
import javax.xml.bind.annotation.XmlElement

//TODO: review this file

class MasterDataKDEString /*: MasterDataKDEBase, IMasterDataKDE*/ {

    var ValueType: Type = String::class.java
    var Value: String? = null
    var Type: String? = null
    var Attributes: MutableMap<String, String> = mutableMapOf()


    constructor() {

    }

    constructor(ns: String, name: String) {
        //this.Namespace = ns;
        //this.Name = name;
    }


    fun GetJson(): JsonToken? {
        TODO("Not yet implemented")
    }

    fun GetXml(): XmlElement? {
        TODO("Not yet implemented")
    }

    fun SetFromJson(json: JsonToken){
        TODO("Not yet implemented")
    }

    fun SetFromXml(xml: XmlElement){
        TODO("Not yet implemented")
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

