package opentraceability.mappers

import opentraceability.interfaces.IEventKDE
import javax.xml.bind.annotation.*
import opentraceability.models.identifiers.*
import opentraceability.models.events.*
import opentraceability.models.events.EPCISVersion
import java.lang.reflect.Type

class OpenTraceabilityXmlMapper {
    companion object {
        fun ToXml(xname: String, value: Object?, version: EPCISVersion, required: Boolean = false): XmlElement? {
            TODO("Not yet implemented")
        }

        fun <T> FromXml(x: XmlElement, version: EPCISVersion): T {
            TODO("Not yet implemented")
        }

        fun FromXml(x: XmlElement, type: Type, version: EPCISVersion): Object {
            TODO("Not yet implemented")
        }

        internal fun WriteObjectToString(obj: Object): String? {
            TODO("Not yet implemented")
        }

        internal fun ReadPropertyMapping(
            mappingProp: OTMappingTypeInformationProperty,
            xchild: XmlElement,
            value: Object,
            version: EPCISVersion
        ) {
            TODO("Not yet implemented")
        }

        internal fun ReadObjectFromString(value: String, t: Type): Object {
            TODO("Not yet implemented")
        }

        internal fun ReadKDE(x: XmlElement): IEventKDE {
            TODO("Not yet implemented")
        }

        internal fun ReadKDE(x: XmlAttribute): IEventKDE {
            TODO("Not yet implemented")
        }
    }
}
