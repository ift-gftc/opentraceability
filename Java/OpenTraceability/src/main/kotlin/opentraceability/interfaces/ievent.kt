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
    var Location: EventLocation?
    var ReadPoint: EventReadPoint?
    var BizTransactionList: MutableList<EventBusinessTransaction>
    var SourceList: MutableList<EventSource>
    var DestinationList: MutableList<EventDestination>
    var KDEs: MutableList<IEventKDE>
    var SensorElementList: MutableList<SensorElement>
    var Products: MutableList<EventProduct>
    var CertificationList: CertificationList?

    fun <T: IEventKDE> GetKDE(clazz: Class<T>, ns: String, name: String): T?
    fun <T: IEventKDE> GetKDE(clazz: Class<T>): T?
    fun AddProduct(product: EventProduct)
    fun GetILMD(): EventILMD?
}
