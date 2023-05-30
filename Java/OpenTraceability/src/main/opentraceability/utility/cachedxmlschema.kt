package utility

import javax.xml.validation.Schema
import java.time.ZonedDateTime
import java.time.ZoneOffset

class CachedXmlSchema {
    var url: String? = null
    var schemaSet: Schema? = null
    val lastUpdated: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)

}
