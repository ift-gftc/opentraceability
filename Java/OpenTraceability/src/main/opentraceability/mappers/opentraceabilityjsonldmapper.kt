package mappers

import com.fasterxml.jackson.core.JsonToken
import interfaces.IEventKDE
import java.util.*
import java.lang.reflect.Type

class OpenTraceabilityJsonLDMapper {
    companion object {
    }

    fun ToJson(value: Object, namespacesReversed: Map<String, String>, required: Boolean): JsonToken? {
        TODO("Not yet implemented")
    }

    fun <T> FromJson(json: JsonToken, namespaces: Map<String, String>): T {
        TODO("Not yet implemented")
    }

    fun FromJson(json: JsonToken, type: Type, namespaces: Map<String, String>): Object {
        TODO("Not yet implemented")
    }

    internal fun WriteObjectToJToken(obj: Object?): JsonToken? {
        TODO("Not yet implemented")
    }
    internal fun ReadPropertyMapping(mappingProp: OTMappingTypeInformationProperty,json: JsonToken, value: Object, namespaces: Map<String, String>) {
        TODO("Not yet implemented")
    }
    internal fun ReadObjectFromString(value: String,t: Type): Object {
        TODO("Not yet implemented")
    }
    internal fun ReadKDE(name: String, json: JsonToken, namespaces: Map<String, String>): IEventKDE {
        TODO("Not yet implemented")
    }
}
