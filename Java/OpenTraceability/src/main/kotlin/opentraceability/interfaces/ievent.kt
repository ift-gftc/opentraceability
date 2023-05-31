package interfaces

import models.events.kdes.CertificationList
import models.events.*
import java.time.*
import java.net.URI

interface IEvent {

    var EventID: URI?
    var CertificationInfo: String?
    var EventTime: OffsetDateTime?
    var EventTimeZoneOffset: Duration?
    var RecordTime: OffsetDateTime?
    var EventType: EventType
    var Action: EventAction?
    var BusinessStep: URI?
    var Disposition: URI?
    var PersistentDisposition: PersistentDisposition?
    var ErrorDeclaration: ErrorDeclaration?
    var Location: EventLocation
    var ReadPoint: EventReadPoint
    var BizTransactionList: ArrayList<EventBusinessTransaction>
    var SourceList: ArrayList<EventSource>
    var DestinationList: ArrayList<EventDestination>
    var KDEs: ArrayList<IEventKDE>
    var SensorElementList: ArrayList<SensorElement>
    var Products: ArrayList<EventProduct>
    var CertificationList: CertificationList?

    fun <T> GetKDE(ns: String, name: String): T
    fun <T> GetKDE(): T
    fun AddProduct(product: EventProduct)
    fun GetILMD(): EventILMD?
}
