package utility

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.lang.reflect.Type

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.JsonSerializer.None
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.util.StdConverter
import java.io.IOException

class MeasurementConverter : JsonDeserializer<Measurement>(), JsonSerializer<Measurement>() {
    @Throws(IOException::class)
    override fun serialize(
        value: Measurement?,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        val strValue = value?.toString()
        if (strValue != null) {
            gen.writeString(strValue)
        }
    }

    @Throws(IOException::class)
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Measurement? {
        return if (p.currentToken == JsonToken.VALUE_STRING) {
            val strValue = p.valueAsString
            if (strValue != null) {
                Measurement.tryParse(strValue)
            } else {
                null
            }
        } else {
            null
        }
    }
}
