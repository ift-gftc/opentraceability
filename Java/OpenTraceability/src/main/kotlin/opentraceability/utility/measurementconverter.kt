package utility

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser

import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.json.JSONObject
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
        return if (p.currentToken == JSONObject.VALUE_STRING) {
            val strValue = p.valueAsString
            if (strValue != null) {
                Measurement.TryParse(strValue)
            } else {
                null
            }
        } else {
            null
        }
    }
}
