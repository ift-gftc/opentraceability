package opentraceability.utility

import com.google.gson.JsonSerializer
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import opentraceability.models.identifiers.GLN
import java.lang.reflect.Type
import com.google.gson.*

class GLNConverter : JsonSerializer<GLN>, JsonDeserializer<GLN> {
    override fun serialize(src: GLN?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val strValue = src?.toString()
        return JsonPrimitive(strValue)
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): GLN? {
        val strValue = json?.asString
        return if (strValue != null) GLN(strValue) else null
    }
}