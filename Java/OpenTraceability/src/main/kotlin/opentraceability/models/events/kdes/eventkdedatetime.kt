package models.events.kdes

import interfaces.IEventKDE
import org.json.JSONObject
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


    override fun getJson(): JSONObject? {
        TODO("Not yet implemented")
    }

    override fun getXml(): Element? {
        TODO("Not yet implemented")
    }

    override fun setFromJson(json: JSONObject) {
        TODO("Not yet implemented")
    }

    override fun setFromXml(xml: Element) {
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
