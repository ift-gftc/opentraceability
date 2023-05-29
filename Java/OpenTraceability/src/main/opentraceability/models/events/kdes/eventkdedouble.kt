package models.events.kdes

import com.fasterxml.jackson.core.JsonToken
import interfaces.IEventKDE
import java.lang.reflect.Type
import javax.xml.bind.annotation.XmlElement

class EventKDEDouble: EventKDEBase, IEventKDE {

    override var ValueType: Type = Double::class.java

    var Value: Double? = null

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

    override fun SetFromJson(json: JsonToken) {
        TODO("Not yet implemented")
    }

    override fun SetFromXml(xml: XmlElement) {
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
