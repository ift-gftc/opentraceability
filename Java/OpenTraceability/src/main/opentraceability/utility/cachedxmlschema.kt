package utility

import com.google.type.DateTime
import java.time.LocalDateTime
import javax.xml.bind.annotation.XmlSchema

class CachedXmlSchema {
    var url: String? = null
    var schemaSet: XmlSchemaSet? = null
    val lastUpdated: DateTime = DateTime.UtcNow
}
