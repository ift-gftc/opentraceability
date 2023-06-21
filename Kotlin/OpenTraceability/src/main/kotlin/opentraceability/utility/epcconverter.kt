package opentraceability.utility

import com.google.gson.JsonSerializer
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import opentraceability.models.identifiers.*
import opentraceability.models.identifiers.EPC
import java.lang.reflect.Type

import com.google.gson.*
import kotlin.reflect.KType

class EPCConverter : JsonSerializer<EPC>, JsonDeserializer<EPC> {
    override fun serialize(src: EPC?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val strValue = src?.toString()
        return JsonPrimitive(strValue)
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): EPC? {
        val strValue = json?.asString
        return if (strValue != null) EPC(strValue) else null
    }
}
