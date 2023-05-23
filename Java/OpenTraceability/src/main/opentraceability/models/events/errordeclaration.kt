package models.events
import interfaces.IEventKDE
import java.util.*
import java.net.URI
import java.time.OffsetDateTime
class ErrorDeclaration {
    var Reason: URI? = null
    var DeclarationTime: OffsetDateTime? = null
    var CorrectingEventIDs: List<String> = ArrayList<String>()
    var ExtensionKDEs: List<IEventKDE> = ArrayList<IEventKDE>()
    companion object{
    }
}
