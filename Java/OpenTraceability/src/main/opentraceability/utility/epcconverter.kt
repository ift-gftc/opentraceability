package utility

import com.google.gson.JsonSerializer
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import models.identifiers.*
import java.lang.reflect.Type


class EPCConverter  /*: JsonConverter<EPC>*/ {

    fun WriteJson(writer: JsonWriter, value: EPC?, serializer: JsonSerializer<Any>) {
        TODO("Not yet implemented")
    }

    fun ReadJson(
        reader: JsonReader,
        objectType: Type,
        existingValue: EPC?,
        hasExistingValue: Boolean,
        serializer: JsonSerializer<Any>
    ): EPC? {
        TODO("Not yet implemented")
    }

}
