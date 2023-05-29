package mappers.epcis.xml

import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlElement
import interfaces.IEvent
import models.events.*
import models.events.EPCISBaseDocument
import models.events.EPCISVersion
import utility.XmlSchemaChecker
import java.lang.reflect.Type

class EPCISDocumentBaseXMLMapper {
    companion object {

        var _schemaChecker: XmlSchemaChecker = XmlSchemaChecker()

        inline fun <reified T : Any> ReadXml(strValue: String, xDoc: XmlDocument): T {
            TODO("Not yet implemented")
        }

        //TODO:epcisNS is XNamespace
        fun WriteXml(doc: EPCISBaseDocument, epcisNS: String, rootEleName: String): XmlDocument {
            TODO("Not yet implemented")
        }


        internal fun GetEventTypeFromProfile(xEvent: XmlElement): Type {
            TODO("Not yet implemented")
        }

        internal fun GetEventXName(e: IEvent): String {
            TODO("Not yet implemented")
        }


        fun ValidateEPCISDocumentSchema(xdoc: XmlDocument, version: EPCISVersion) {
            TODO("Not yet implemented")
        }

        fun ValidateEPCISQueryDocumentSchema(xdoc: XmlDocument, version: EPCISVersion) {
            TODO("Not yet implemented")
        }
    }
}
