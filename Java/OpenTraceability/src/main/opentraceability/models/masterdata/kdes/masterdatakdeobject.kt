package models.masterdata.kdes

import com.fasterxml.jackson.core.JsonToken
import interfaces.IEventKDE
import models.events.kdes.EventKDEBase
import java.lang.reflect.Type
import javax.xml.bind.annotation.XmlElement

class MasterDataKDEObject: EventKDEBase, IEventKDE {

    override var ValueType: Type = Boolean::class.java

    var Value: Boolean? = null

    constructor() {

    }

    constructor(ns: String, name: String) {
        this.Namespace = ns;
        this.Name = name;
    }


    override fun GetJson(): JsonToken? {
        TODO("Not yet implemented")
    }

    override fun GetXml(): XmlElement? {
        TODO("Not yet implemented")
    }

    override fun SetFromJson(json: JsonToken): Void {
        TODO("Not yet implemented")
    }

    override fun SetFromXml(xml: XmlElement): Void {
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
