package opentraceability.interfaces

import opentraceability.models.events.*
import opentraceability.models.events.EPCISQueryDocument

interface IEPCISQueryDocumentMapper {
    fun Map(strValue: String, checkSchema: Boolean = true): EPCISQueryDocument
    fun Map(doc: EPCISQueryDocument): String
}
