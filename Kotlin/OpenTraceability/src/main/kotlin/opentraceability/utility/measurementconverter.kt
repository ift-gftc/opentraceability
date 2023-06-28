package opentraceability.utility

import com.google.gson.JsonSerializer
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import opentraceability.models.identifiers.GLN
import java.lang.reflect.Type
import com.google.gson.*

class MeasurementConverter : JsonDeserializer<Measurement>, JsonSerializer<Measurement>
{
    override fun serialize(src: Measurement?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement
    {
        val strValue = src?.toString()
        return JsonPrimitive(strValue)
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Measurement?
    {
        val strValue = json?.asString
        return if (strValue != null) Measurement.TryParse(strValue) else null
    }
}
