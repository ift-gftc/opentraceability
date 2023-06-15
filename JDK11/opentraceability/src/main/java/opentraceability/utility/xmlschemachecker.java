package opentraceability.utility;

import opentraceability.OTLogger;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class XmlSchemaChecker {
    private static final Map<String, CachedXmlSchema> cache = new HashMap<>();
    private static final Object lock = new Object();

    public static Pair<Boolean, String> validate(Document xml, String schemaURL) {
        StringBuilder validationError = new StringBuilder();
        String error = null;
        boolean isFileOk = false;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource reader = new InputSource();
            reader.setCharacterStream(new StringReader(xml.toString()));

            Document doc = builder.parse(reader);

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            URL schemaURLObject = new URL(schemaURL);
            Schema schema = schemaFactory.newSchema(schemaURLObject);

            javax.xml.validation.Validator validator = schema.newValidator();
            ErrorHandler errorHandler = new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) {
                    validationError.append("Warning: Matching schema not found. No validation occurred. ");
                    if (exception != null) {
                        validationError.append(exception.getMessage());
                    }
                }

                @Override
                public void error(SAXParseException exception) {
                    validationError.append("Validation error: ");
                    if (exception != null) {
                        validationError.append(exception.getMessage());
                    }
                }

                @Override
                public void fatalError(SAXParseException exception) {
                    validationError.append("Validation error: ");
                    if (exception != null) {
                        validationError.append(exception.getMessage());
                    }
                }
            };
            validator.setErrorHandler(errorHandler);
            validator.validate(new DOMSource(doc));

            isFileOk = true;

        } catch (FileNotFoundException e) {
            OTLogger.error(e);
        } catch (Exception e) {
            OTLogger.error(e);
        } finally {
            error = validationError.toString();
            isFileOk = error == null || error.isBlank();
        }

        return new Pair<>(isFileOk, error);
    }

    public static Schema getSchema(String url) {
        Date now = new Date();

        CachedXmlSchema cacheEntry = cache.get(url);
        Duration timeDifference = Duration.between(cacheEntry.getLastUpdated(), Instant.now());
        long hoursDifference = timeDifference.toHours();
        boolean isCacheExpired = hoursDifference > 1;

        if (!cache.containsKey(url) || isCacheExpired) {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            Schema schema;
            try {
                StreamSource reader2 = new StreamSource(new URL(url).openStream());
                schema = factory.newSchema(reader2);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load the schema from the URL " + url, e);
            }

            CachedXmlSchema cachedSchema = new CachedXmlSchema(url, schema);
            cache.put(url, cachedSchema);
        }

        return cache.get(url).getSchemaSet();
    }
}