package utility

import com.google.type.DateTime

class CachedXmlSchema {
    var LastUpdated: DateTime = DateTime()
    var URL: String = String()
    var SchemaSet: XmlSchemaSet = XmlSchemaSet()
    companion object{
    }
}
