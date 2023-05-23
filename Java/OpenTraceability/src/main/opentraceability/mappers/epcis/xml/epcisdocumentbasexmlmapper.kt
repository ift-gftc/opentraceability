package mappers.epcis.xml

import com.intellij.psi.xml.XmlDocument
import interfaces.IEPCISDocumentMapper
import interfaces.IEvent
import models.events.*
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.typeOf

class EPCISDocumentBaseXMLMapper  {
    companion object {
    }


    inline fun <reified T : Any> ReadXml(strValue: String, xDoc: XmlDocument?): T? {
        TODO("Not yet implemented")
        return T::class.primaryConstructor!!.call()
    }

    fun WriteXml(doc: EPCISBaseDocument, epcisNS: String, rootEleName: String): XmlDocument {
        TODO("Not yet implemented")
    }


    internal fun GetEventTypeFromProfile(xdoc: XmlDocument): KType {
        TODO("Not yet implemented")
        return typeOf<String>()
    }

    internal fun GetEventXName(e: IEvent): String {
        TODO("Not yet implemented")
        return ""
    }


    fun ValidateEPCISDocumentSchema(xdoc: XmlDocument, version: EPCISVersion) {
        TODO("Not yet implemented")
    }

    fun ValidateEPCISQueryDocumentSchema(xdoc: XmlDocument, version: EPCISVersion) {
        TODO("Not yet implemented")
    }

}
