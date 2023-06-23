package opentraceability.interfaces

import opentraceability.models.events.*

interface IEPCISQueryDocumentMapper {
    fun map(strValue: String, checkSchema: Boolean = true): EPCISQueryDocument
    fun map(doc: EPCISQueryDocument): String
}
