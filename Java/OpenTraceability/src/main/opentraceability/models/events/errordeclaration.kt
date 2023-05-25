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
    var CorrectingEventIDs: ArrayList<String>? = null

    //[OpenTraceabilityExtensionElements]
    var ExtensionKDEs: ArrayList<IEventKDE> = ArrayList<IEventKDE>()

}
