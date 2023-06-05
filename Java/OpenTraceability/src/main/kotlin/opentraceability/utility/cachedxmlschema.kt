package opentraceability.utility

import javax.xml.validation.Schema
import java.time.ZonedDateTime
import java.time.ZoneOffset

class CachedXmlSchema {
    var url: String? = null
    var schemaSet: Schema? = null
    val lastUpdated: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)

    constructor(_url: String, _schemaSet: Schema){
        url = _url
        schemaSet = _schemaSet
    }

}