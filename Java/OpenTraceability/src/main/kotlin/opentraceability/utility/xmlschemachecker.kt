package opentraceability.utility

import org.w3c.dom.Document
import java.net.URL
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock
import javax.sql.rowset.spi.XmlReader
import javax.xml.XMLConstants
import javax.xml.transform.dom.DOMSource
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory
import javax.xml.validation.Validator


class XmlSchemaChecker {

    companion object {
        private val cache: MutableMap<String, Schema> = mutableMapOf()
        private val lock = Object()

        fun validate(xml: Document, schemaURL: String, error: AtomicReference<String?>): Boolean
        {
            error.set(null);
            try
            {
                // Create a validator from the schema
                val validator = getSchemaValidator(schemaURL)
                    ?: throw Exception("Failed to get validator for schema URL: $schemaURL")

                // Validate the XML document using the validator
                validator.validate(DOMSource(xml))

                // The XML document is valid against the schema
                return true
            }
            catch (e: Exception)
            {
                // The XML document is not valid against the schema
                error.set(e.stackTraceToString())
                return false
            }
        }

        private fun getSchemaValidator(url: String): Validator?
        {
            synchronized(lock)
            {
                if (!cache.containsKey(url)) {
                    // Create a SchemaFactory for XML schema (XSD)
                    val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)

                    // Load the XML schema from the URL
                    val schema = schemaFactory.newSchema(URL(url))

                    cache[url] = schema;
                }
            }

            return cache[url]?.newValidator()
        }
    }
}
