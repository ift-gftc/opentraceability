package utility

import com.intellij.psi.xml.XmlDocument
import javax.xml.bind.annotation.XmlSchema

class XmlSchemaChecker {

    var _cache: MutableMap<String, CachedXmlSchema> = mutableMapOf()

    fun Validate(xml: XmlDocument, schemaURL: String, error: String?): Boolean {
        TODO("Not yet implemented")
    }

    fun GetSchema(url: String): XmlSchema? {
        TODO("Not yet implemented")
    }

}
