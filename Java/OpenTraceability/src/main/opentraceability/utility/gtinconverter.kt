package opentraceability.utility

import com.google.gson.JsonSerializer
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import opentraceability.models.identifiers.GTIN
import java.lang.reflect.Type

class GTINConverter /*: JsonConverter<GTIN>*/ {

    fun WriteJson(writer: JsonWriter, value: GTIN?, serializer: JsonSerializer<Any>) {
        TODO("Not yet implemented")
    }

    fun ReadJson(
        reader: JsonReader,
        objectType: Type,
        existingValue: GTIN?,
        hasExistingValue: Boolean,
        serializer: JsonSerializer<Any>
    ): GTIN? {
        TODO("Not yet implemented")
    }

}
