package utility

import com.google.gson.*
import models.identifiers.GTIN

class GTINConverter : JsonSerializer<GTIN>, JsonDeserializer<GTIN> {
    override fun serialize(src: GTIN?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val strValue = src?.toString()
        return JsonPrimitive(strValue)
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): GTIN? {
        val strValue = json?.asString
        return if (strValue != null) GTIN(strValue) else null
    }
}
