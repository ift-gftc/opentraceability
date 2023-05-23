package models.events
import interfaces.IEventKDE
import java.util.*
import models.identifiers.*
import models.events.kdes.CertificationList
import java.time.Duration
import java.net.URI
import java.time.OffsetDateTime
class EventBase {
    var EventTime: OffsetDateTime? = null
    var RecordTime: OffsetDateTime? = null
    var EventTimeZoneOffset: Duration? = null
    var EventID: URI? = null
    var ErrorDeclaration: ErrorDeclaration = ErrorDeclaration()
    var CertificationInfo: String = String()
    var CertificationList: CertificationList = CertificationList()
    var InformationProvider: PGLN = PGLN()
    var KDEs: List<IEventKDE> = ArrayList<IEventKDE>()
    companion object{
    }
}
