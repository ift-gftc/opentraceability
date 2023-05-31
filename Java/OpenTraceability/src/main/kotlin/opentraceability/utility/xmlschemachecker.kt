package utility

import org.w3c.dom.Document
import javax.sql.rowset.spi.XmlReader


class XmlSchemaChecker {
    private val cache: MutableMap<String, CachedXmlSchema> = mutableMapOf()

    fun validate(xml: Document, schemaURL: String, error: AtomicReference<String?>): Boolean {
        val validationError = StringBuilder()
        error.set(null)

        var bFileOk = false
        var stringReader: TextReader? = null
        var reader: XmlReader? = null

        try {
            val settings = XmlReaderSettings()
            settings.validationType = ValidationType.None
            settings.validationEventHandler = ValidationEventHandler { _, args ->
                if (args.severity == XmlSeverityType.Warning) {
                    validationError.appendLine("Warning: Matching schema not found. No validation occurred. ${args.message}")
                } else {
                    validationError.appendLine("Validation error: ${args.message}")
                }
            }

            settings.validationType = ValidationType.Schema
            settings.schemas = getSchema(schemaURL) ?: throw Exception("Failed to load schema with URL $schemaURL")

            var bOk = true

            stringReader = stringReader(xml.toString())
            reader = XmlReader.create(stringReader, settings)

            do {
                bOk = reader.read()
            } while (bOk)

            reader.close()
            reader.dispose()
            reader = null

            bFileOk = true
        } catch (ex: OperationCanceledException) {
            throw ex
        } catch (notFoundException: FileNotFoundException) {
            OTLogger.error(notFoundException)
        } catch (ex: Exception) {
            OTLogger.error(ex)
        } finally {
            if (reader != null) {
                reader.close()
                reader.dispose()
                reader = null
            }

            if (stringReader != null) {
                stringReader.close()
                stringReader.dispose()
                stringReader = null
            }

            error.set(validationError.toString())
            bFileOk = error.get()?.isNullOrBlank() ?: false
        }

        return bFileOk
    }

    fun getSchema(url: String): XmlSchemaSet? {
        if (!cache.containsKey(url) || (DateTime.UtcNow - cache[url]?.lastUpdated).totalHours > 1) {
            val sc = XmlSchemaSet()
            sc.xmlResolver = XmlUrlResolver()

            val schema = XmlSchema.read(XmlReader.create(url), ValidationEventHandler { _, e -> println(e.message) })

            if (schema == null) throw NullPointerException("Failed to load the schema from the URL $url")

            sc.add(schema)

            val cachedSchema = CachedXmlSchema()
            cachedSchema.url = url
            cachedSchema.schemaSet = sc
            cache[url] = cachedSchema
        }

        return cache[url]?.schemaSet
    }
}
