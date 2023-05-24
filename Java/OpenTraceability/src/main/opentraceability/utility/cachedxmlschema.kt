package utility

import java.time.LocalDateTime
import javax.xml.bind.annotation.XmlSchema

class CachedXmlSchema {
    var LastUpdated: LocalDateTime = LocalDateTime.now()
    var URL: String = String()
    var SchemaSet: XmlSchema? = null
}
