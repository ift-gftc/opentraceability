package models.events.kdes

import com.fasterxml.jackson.core.JsonToken
import interfaces.IEventKDE
import org.json.simple.JSONObject
import java.lang.reflect.Type
import java.time.OffsetDateTime
import org.w3c.dom.Element

class EventKDEDateTime: EventKDEBase, IEventKDE {

    override var ValueType: Type = OffsetDateTime::class.java

    var Value: OffsetDateTime? = null

    constructor(ns: String, name: String) {
        this.Namespace = ns;
        this.Name = name;
    }


    override fun GetJson(): JSONObject? {
        TODO("Not yet implemented")
    }

    override fun GetXml(): Element? {
        TODO("Not yet implemented")
    }

    override fun SetFromJson(json: JSONObject) {
        TODO("Not yet implemented")
    }

    override fun SetFromXml(xml: Element) {
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
