package opentraceability.utility

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.exceptions.ProcessingException
import com.github.fge.jsonschema.main.JsonSchema
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.worldturner.medeia.schema.validation.SchemaValidator
import kotlinx.coroutines.runBlocking
import org.everit.json.schema.Validator
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

object JsonSchemaChecker {
    private val schemaCache: MutableMap<String, JsonNode> = mutableMapOf()

    fun isValid(jsonStr: String, schemaURL: String, errors: MutableList<String>): Boolean {
        val schemaNode: JsonNode = getSchema(schemaURL)
        val objectMapper = ObjectMapper()
        val jsonNode: JsonNode = objectMapper.readTree(jsonStr)
        val schema: JsonSchema = JsonSchemaFactory.byDefault().getJsonSchema(schemaNode)

        return try {
            schema.validate(jsonNode)
            return true
        } catch (e: ProcessingException) {
            errors.add(e.message ?: "")
            return false
        }
    }


    private fun getSchema(schemaUrl: String): JsonNode {
        return schemaCache.getOrPut(schemaUrl) {
            val schemaJson: String = URL(schemaUrl).readText()
            JsonLoader.fromString(schemaJson)
        }
    }
}

