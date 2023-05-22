package interfaces

import models.events.kdes.CertificationList
import models.events.*
import java.time.Duration
import java.net.URI
import java.time.OffsetDateTime

interface IEvent {

    var EventID: URI?
    var CertificationInfo: String?
    var EventTime: OffsetDateTime?
    var EventTimeZoneOffset: Duration?
    var RecordTime: OffsetDateTime?
    var EventType: EventType?
    var Action: EventAction?
    var BusinessStep: URI?
    var Disposition: URI?
    var PersistentDisposition: PersistentDisposition?
    var ErrorDeclaration: ErrorDeclaration?
    var Location: EventLocation
    var ReadPoint: EventReadPoint
    var BizTransactionList: List<EventBusinessTransaction>
    var SourceList: List<EventSource>
    var DestinationList: List<EventDestination>
    var KDEs: List<IEventKDE>
    var SensorElementList: List<SensorElement>
    var Products: List<EventProduct>
    var CertificationList: CertificationList?

    fun <T> GetKDE(ns: String, name: String): T
    fun <T> GetKDE(): T
    fun AddProduct(product: EventProduct): Void
    fun GetILMD(): EventILMD?
}
