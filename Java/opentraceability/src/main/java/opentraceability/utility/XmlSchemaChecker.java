package opentraceability.utility;

import opentraceability.OTLogger;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import tangible.StringHelper;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class XmlSchemaChecker {
    private static final Map<String, CachedXmlSchema> cache = new HashMap<>();
    private static final Object lock = new Object();

    public static Boolean validate(XElement xml, String schemaURL, tangible.OutObject<String> error) {
        StringBuilder validationError = new StringBuilder();
        boolean isFileOk = false;

        try {

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = getSchema(schemaURL);

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
            validator.validate(new DOMSource(xml.element.getOwnerDocument()));

            error.outArgValue = validationError.toString();
        } catch (FileNotFoundException e) {
            error.outArgValue = e.getMessage();
        } catch (Exception e) {
            error.outArgValue = e.getMessage();
        } finally {
            isFileOk = StringHelper.isNullOrEmpty(error.outArgValue);
        }

        return isFileOk;
    }

    public static Schema getSchema(String url) {
        Date now = new Date();

        CachedXmlSchema cacheEntry = cache.get(url);
        boolean isCacheExpired = true;
        if (cacheEntry != null) {
            Duration timeDifference = Duration.between(cacheEntry.lastUpdated, OffsetDateTime.now(ZoneOffset.UTC));
            long hoursDifference = timeDifference.toHours();
            isCacheExpired = hoursDifference > 1;
        }

        if (isCacheExpired) {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            Schema schema;
            try
            {
                URL resourceURL = XmlSchemaChecker.class.getResource(url);
                schema = factory.newSchema(resourceURL);
            }
            catch (Exception e)
            {
                throw new RuntimeException("Failed to load the schema from the URL " + url, e);
            }

            CachedXmlSchema cachedSchema = new CachedXmlSchema(url, schema);
            cache.put(url, cachedSchema);
        }

        return cache.get(url).schemaSet;
    }
}