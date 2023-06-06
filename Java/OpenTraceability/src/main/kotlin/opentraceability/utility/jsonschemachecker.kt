package opentraceability.utility

import com.networknt.schema.*
import com.fasterxml.jackson.databind.*
import java.net.URL
import java.net.http.*
import java.util.concurrent.ConcurrentHashMap

object JsonSchemaChecker {
    private val lock = Any()
    private val schemaCache = ConcurrentHashMap<String, String>()

    fun isValid(jsonStr: String, schemaURL: String): Pair<Boolean, List<String>> {
        var schemaStr: String? = schemaCache[schemaURL]
        if (schemaStr == null) {
            synchronized(lock) {
                val client = HttpClient.newHttpClient()
                val request = HttpRequest.newBuilder()
                    .uri(URL(schemaURL).toURI())
                    .build()
                schemaStr = client.send(request, HttpResponse.BodyHandlers.ofString()).body()
                schemaCache.put(schemaURL, schemaStr!!)
            }
        }

        val objectMapper = ObjectMapper()

        val mySchema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4).getSchema(schemaStr)
        val results = mySchema.validate(objectMapper.readTree(jsonStr))

        return if (results.isEmpty()) {
            Pair(true, emptyList())
        } else {
            Pair(false, results.map { "${it.message} :: ${it.type}" })
        }
    }
}
