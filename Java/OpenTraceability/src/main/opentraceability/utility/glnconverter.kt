package utility

import com.google.gson.JsonSerializer
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import models.identifiers.GLN
import java.lang.reflect.Type

class GLNConverter /*: JsonConverter<GLN>*/ {

    fun WriteJson(writer: JsonWriter, value: GLN?, serializer: JsonSerializer<Any>) {
        TODO("Not yet implemented")
    }

    fun ReadJson(
        reader: JsonReader,
        objectType: Type,
        existingValue: GLN?,
        hasExistingValue: Boolean,
        serializer: JsonSerializer<Any>
    ): GLN? {
        TODO("Not yet implemented")
    }

}
