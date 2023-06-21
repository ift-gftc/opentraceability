package opentraceability.models.events

import opentraceability.interfaces.IEventKDE
import opentraceability.utility.attributes.*
import java.util.*
import java.net.URI
import java.time.OffsetDateTime

class ErrorDeclaration {

    @OpenTraceabilityJsonAttribute("reason")
    @OpenTraceabilityAttribute("", "@type")
    var Reason: URI? = null

    @OpenTraceabilityAttribute("", "declarationTime")
    var DeclarationTime: OffsetDateTime? = null

    @OpenTraceabilityArrayAttribute("correctiveEventID")
    @OpenTraceabilityAttribute("", "correctiveEventIDs")
    var CorrectingEventIDs: MutableList<String>? = null

    @OpenTraceabilityExtensionElementsAttribute
    var ExtensionKDEs: MutableList<IEventKDE> = mutableListOf()

}
