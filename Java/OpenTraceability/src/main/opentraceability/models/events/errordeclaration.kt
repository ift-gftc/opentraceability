package models.events
import interfaces.IEventKDE
import java.util.*
import java.net.URI
import java.time.OffsetDateTime
class ErrorDeclaration {

    //TODO: review this file

    //[OpenTraceabilityJson("reason")]
    //[OpenTraceability("@type")]
    var Reason: URI? = null

    //[OpenTraceability("declarationTime")]
    var DeclarationTime: OffsetDateTime? = null

    //[OpenTraceabilityArray("correctiveEventID")]
    //[OpenTraceability("correctiveEventIDs")]
    var CorrectingEventIDs: List<String>? = null

    //[OpenTraceabilityExtensionElements]
    var ExtensionKDEs: List<IEventKDE> = ArrayList<IEventKDE>()

}
