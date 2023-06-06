package opentraceability.utility

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.contentOrNull
import java.net.URL
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.map

object JsonSchemaChecker {
    private val schemaCache: MutableMap<String, String> = mutableMapOf()
    private val lock: Any = Any()

    fun isValid(jsonStr: String, schemaURL: String, errors: MutableList<String>): Boolean {
        var schemaStr: String? = schemaCache[schemaURL]
        if (schemaStr == null) {
            synchronized(lock) {
                if (!schemaCache.containsKey(schemaURL)) {
                    schemaStr = retrieveSchema(schemaURL)
                    schemaCache[schemaURL] = schemaStr!!
                }
            }
        }

        val jDoc = JsonDocument(jsonStr)
        val mySchema = JsonSchema.fromText(schemaStr!!)
        val results = mySchema.evaluate(jDoc, EvaluationOptions(outputFormat = OutputFormat.List))

        return if (results.isValid) {
            errors.clear()
            true
        } else {
            errors.clear()
            errors.addAll(results.errors.map { "${it.key} :: ${it.value}" })
            errors.addAll(results.details.flatMap { it.errors?.values?.map { e -> "${e.key} :: ${e.value}" } ?: emptyList() })
            false
        }
    }

    private fun retrieveSchema(schemaURL: String): String {
        val url = URL(schemaURL)
        val connection = url.openConnection()
        val schemaStr = connection.getInputStream().use { it.reader().readText() }
        return schemaStr
    }
}

class JsonSchema(private val schema: JsonObject) {
    fun evaluate(json: JsonDocument, options: EvaluationOptions): EvaluationResult {
        // Replace the code below with the logic to evaluate the given JSON against the schema
        // and return the evaluation result
        return EvaluationResult(isValid = false)
    }

    companion object {
        fun fromText(schemaStr: String): JsonSchema {
            val schemaObj = Json.decodeFromString<JsonObject>(schemaStr)
            return JsonSchema(schemaObj)
        }
    }
}

class EvaluationOptions(val outputFormat: OutputFormat)

class EvaluationResult(val isValid: Boolean, val errors: List<Error> = emptyList(), val details: List<Detail> = emptyList())

class Error(val key: String, val value: String)

class Detail(val errors: Map<String, Error>?)

enum class OutputFormat {
    List
}

class JsonDocument(private val jsonString: String) {
    private val json: JsonObject = Json.decodeFromString<JsonObject>(jsonString)

    fun getString(key: String): String? {
        return json[key]?.jsonPrimitive?.contentOrNull
    }

    fun getObject(key: String): JsonDocument? {
        val obj = json[key]?.jsonObject
        return if (obj != null) {
            JsonDocument(obj.toString())
        } else {
            null
        }
    }

    fun getList(key: String): List<JsonDocument>? {
        val list = json[key]?.jsonArray
        return if (list != null) {
            list.map { JsonDocument(it.toString()) }
        } else {
            null
        }
    }

    fun getPrimitive(key: String): JsonPrimitive? {
        return json[key]?.jsonPrimitive
    }
}
