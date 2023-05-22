package mappers

import interfaces.IEventKDE
import javax.xml.bind.annotation.*
import models.identifiers.*
import models.events.*
import java.lang.reflect.Type

class OpenTraceabilityXmlMapper {
    companion object {
    }

    fun ToXml(xname: String, value: Object, version: EPCISVersion, required: Boolean): XmlElement {
        TODO("Not yet implemented")
    }

    fun <T> FromXml(x: XmlElement, version: EPCISVersion): T {
        TODO("Not yet implemented")
    }

    fun FromXml(x: XmlElement, type: Type, version: EPCISVersion): Object {
        TODO("Not yet implemented")
    }

    internal fun WriteObjectToString(): String? {
        TODO("Not yet implemented")
    }
    internal fun ReadPropertyMapping() {
        TODO("Not yet implemented")
    }
    internal fun ReadObjectFromString(): Object {
        TODO("Not yet implemented")
    }
    internal fun ReadKDE(): IEventKDE {
        TODO("Not yet implemented")
    }
    internal fun ReadKDE(XmlAttribute x): IEventKDE {
        TODO("Not yet implemented")
    }
}
