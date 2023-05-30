package utility

import com.google.gson.JsonSerializer
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import models.identifiers.*
import models.identifiers.PGLN
import java.lang.reflect.Type

import com.google.gson.*

class PGLNConverter : JsonSerializer<PGLN>, JsonDeserializer<PGLN> {
    override fun serialize(src: PGLN?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val strValue = src?.toString()
        return JsonPrimitive(strValue)
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): PGLN? {
        val strValue = json?.asString
        return strValue?.let { PGLN(it) }
    }
}
