package opentraceability.utility

import opentraceability.OTLogger
import org.w3c.dom.Document
import java.net.URL
import javax.xml.XMLConstants
import javax.xml.transform.dom.DOMSource
import javax.xml.validation.SchemaFactory
import org.xml.sax.ErrorHandler
import org.xml.sax.InputSource
import org.xml.sax.SAXParseException
import java.io.FileNotFoundException
import java.io.StringReader
import java.time.Instant
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.Schema
import java.util.Date
import java.time.Duration

class XmlSchemaChecker {

    companion object {
        private val _cache: MutableMap<String, CachedXmlSchema> = mutableMapOf()
        private val lock = Object()

        fun validate(xml: Document, schemaURL: String): Pair<Boolean, String?> {
            val validationError = StringBuilder()
            var error: String? = null
            var isFileOk = false

            try {
                val factory = DocumentBuilderFactory.newInstance()
                factory.isNamespaceAware = true
                val builder = factory.newDocumentBuilder()
                val reader = InputSource()
                reader.characterStream = StringReader(xml.toString())

                val doc = builder.parse(reader)

                val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                val schemaURLObject = URL(schemaURL)
                val schema = schemaFactory.newSchema(schemaURLObject)

                val validator = schema.newValidator()
                val errorHandler = object : ErrorHandler {
                    override fun warning(exception: SAXParseException?) {
                        validationError.appendLine("Warning: Matching schema not found. No validation occurred. ${exception?.message}")
                    }

                    override fun error(exception: SAXParseException?) {
                        validationError.appendLine("Validation error: ${exception?.message}")
                    }

                    override fun fatalError(exception: SAXParseException?) {
                        validationError.appendLine("Validation error: ${exception?.message}")
                    }
                }
                validator.errorHandler = errorHandler
                validator.validate(DOMSource(doc))

                isFileOk = true

            } catch (e: FileNotFoundException) {
                OTLogger.error(e)
            } catch (e: Exception) {
                OTLogger.error(e)
            } finally {
                error = validationError.toString()
                isFileOk = error.isNullOrBlank()
            }

            return Pair(isFileOk, error)
        }

        fun getSchema(url: String): Schema? {
            val now = Date()

            val cacheEntry = _cache[url]
            val timeDifference = Duration.between(cacheEntry?.lastUpdated, Instant.now())
            val hoursDifference = timeDifference.toHours()
            val isCacheExpired = hoursDifference > 1

            if (!_cache.containsKey(url) || isCacheExpired) {
                val factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)

                val schema: Schema?
                try {
                    val reader2 = StreamSource(URL(url).openStream())
                    schema = factory.newSchema(reader2)
                } catch (e: Exception) {
                    throw RuntimeException("Failed to load the schema from the URL $url", e)
                }

                val cachedSchema = CachedXmlSchema(url, schema)
                _cache[url] = cachedSchema
            }

            return _cache[url]?.schemaSet
        }


    }
}
