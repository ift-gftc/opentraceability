package opentraceability.interfaces

import opentraceability.models.events.*

interface IEPCISDocumentMapper {
    fun map(strValue: String): EPCISDocument
    fun map(doc: EPCISDocument): String
}
