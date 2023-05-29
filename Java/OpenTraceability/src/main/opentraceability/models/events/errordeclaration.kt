package opentraceability.models.events
import opentraceability.interfaces.IEventKDE
import opentraceability.utility.attributes.OpenTraceabilityAttribute
import opentraceability.utility.attributes.OpenTraceabilityExtensionElementsAttribute
import opentraceability.utility.attributes.OpenTraceabilityJsonAttribute
import java.util.*
import java.net.URI
import java.time.OffsetDateTime
class ErrorDeclaration {

    @OpenTraceabilityJsonAttribute("reason")
    @OpenTraceabilityAttribute("","@type")
    var Reason: URI? = null

    @OpenTraceabilityAttribute("","declarationTime")
    var DeclarationTime: OffsetDateTime? = null

    //[OpenTraceabilityArray("correctiveEventID")
    @OpenTraceabilityAttribute("","correctiveEventIDs")
    var CorrectingEventIDs: ArrayList<String>? = null

    @OpenTraceabilityExtensionElementsAttribute
    var ExtensionKDEs: ArrayList<IEventKDE> = ArrayList<IEventKDE>()

}
