package utility

import java.io.IOException
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

object JsonSchemaChecker {
    private val schemaCache: MutableMap<String, JsonSchema> = ConcurrentHashMap()

    @Throws(IOException::class, ProcessingException::class)
    fun isValid(jsonStr: String, schemaURL: String): Pair<Boolean, List<String>> {
        val schema: JsonSchema = schemaCache.computeIfAbsent(schemaURL) { loadSchema(schemaURL) }
        val processingReport: ProcessingReport = schema.validate(URI.create(schemaURL), jsonStr)
        return if (processingReport.isSuccess) {
            Pair(true, emptyList())
        } else {
            Pair(false, extractErrors(processingReport))
        }
    }

    @Throws(ProcessingMessageException::class)
    fun extractErrors(processingReport: ProcessingReport): List<String> {
        val errorMessages: MutableList<String> = mutableListOf()
        val iterator: ProcessingReportIterator = toIterable(processingReport).iterator()
        while (iterator.hasNext()) {
            val processingMessage: ProcessingMessage = iterator.next()
            val logLevel: LogLevel = processingMessage.logLevel
            if (logLevel == LogLevel.ERROR || logLevel == LogLevel.FATAL) {
                val exception: ProcessingReportException = ProcessingReportException.from(processingMessage)
                val errorMessage: String = exception.message ?: ""
                errorMessages.add(errorMessage)
            }
        }
        return errorMessages
    }

    @Throws(IOException::class, ProcessingException::class)
    fun loadSchema(schemaURL: String): JsonSchema {
        val schemaFactory: JsonSchemaFactory = JsonSchemaFactory.byDefault()
        return schemaFactory.getJsonSchema(URI(schemaURL))
    }
}
