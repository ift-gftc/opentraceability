package models.events

import interfaces.IEventKDE
import utility.attributes.OpenTraceabilityArrayAttribute
import utility.attributes.OpenTraceabilityAttribute
import utility.attributes.OpenTraceabilityExtensionElementsAttribute
import utility.attributes.OpenTraceabilityJsonAttribute
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
    var CorrectingEventIDs: ArrayList<String>? = null

    @OpenTraceabilityExtensionElementsAttribute
    var ExtensionKDEs: ArrayList<IEventKDE> = ArrayList<IEventKDE>()

}
