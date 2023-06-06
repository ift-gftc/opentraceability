package opentraceability.models.events.kdes

import opentraceability.interfaces.IEventKDE
import org.json.JSONObject
import java.lang.reflect.Type
import java.time.OffsetDateTime
import org.w3c.dom.Element
import kotlin.reflect.KType
import kotlin.reflect.full.createType

class EventKDEDateTime: EventKDEBase, IEventKDE {

    override var valueType: KType = OffsetDateTime::class.createType()

    var value: OffsetDateTime? = null

    constructor(){}


    constructor(ns: String, name: String) {
        this.namespace = ns;
        this.name = name;
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
        if (value != null) {
            return value.toString()
        } else {
            return ""
        }
    }
}
