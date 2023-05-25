package utility

import com.google.gson.JsonSerializer
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.lang.reflect.Type

class MeasurementConverter /*: JsonConverter<Measurement>*/ {

    fun WriteJson(writer: JsonWriter, value: Measurement?, serializer: JsonSerializer<Any>) {
        TODO("Not yet implemented")
    }

    fun ReadJson(reader: JsonReader, objectType: Type, existingValue: Measurement?, hasExistingValue: Boolean, serializer: JsonSerializer<Any>) : Measurement? {
        TODO("Not yet implemented")
    }

}
