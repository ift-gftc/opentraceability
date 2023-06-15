package opentraceability.utility;

import javax.xml.validation.Schema;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class CachedXmlSchema {
    public String url = null;
    public Schema schemaSet = null;
    public ZonedDateTime lastUpdated = ZonedDateTime.now(ZoneOffset.UTC);

    public CachedXmlSchema() {}

    public CachedXmlSchema(String url, Schema schemaSet) {
        this.url = url;
        this.schemaSet = schemaSet;
    }
}