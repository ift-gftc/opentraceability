package interfaces
import models.events.kdes.CertificationList
import models.events.*
import java.time.Duration
import java.lang.reflect.Type
import java.net.URI
import java.time.OffsetDateTime
interface IEvent<T> {
    fun get_EventID(): URI
    fun set_EventID(value: URI): Void
    fun get_CertificationInfo(): String
    fun set_CertificationInfo(value: String): Void
    fun get_EventTime(): OffsetDateTime?
    fun set_EventTime(value: OffsetDateTime?): Void
    fun get_EventTimeZoneOffset(): Duration?
    fun set_EventTimeZoneOffset(value: Duration?): Void
    fun get_RecordTime(): OffsetDateTime?
    fun set_RecordTime(value: OffsetDateTime?): Void
    fun get_EventType(): EventType
    fun get_Action(): EventAction?
    fun set_Action(value: EventAction?): Void
    fun get_BusinessStep(): URI
    fun set_BusinessStep(value: URI): Void
    fun get_Disposition(): URI
    fun set_Disposition(value: URI): Void
    fun get_PersistentDisposition(): PersistentDisposition
    fun set_PersistentDisposition(value: PersistentDisposition): Void
    fun get_ErrorDeclaration(): ErrorDeclaration
    fun set_ErrorDeclaration(value: ErrorDeclaration): Void
    fun get_Location(): EventLocation
    fun set_Location(value: EventLocation): Void
    fun get_ReadPoint(): EventReadPoint
    fun set_ReadPoint(value: EventReadPoint): Void
    fun get_BizTransactionList(): List<EventBusinessTransaction>
    fun set_BizTransactionList(value: List<EventBusinessTransaction>): Void
    fun get_SourceList(): List<EventSource>
    fun set_SourceList(value: List<EventSource>): Void
    fun get_DestinationList(): List<EventDestination>
    fun set_DestinationList(value: List<EventDestination>): Void
    fun get_KDEs(): List<IEventKDE>
    fun get_SensorElementList(): List<SensorElement>
    fun set_SensorElementList(value: List<SensorElement>): Void
    fun get_Products(): List<EventProduct>
    fun<T> GetKDE(ns: String, name: String): T
    fun<T> GetKDE(): T
    fun AddProduct(product: EventProduct): Void
    fun get_CertificationList(): CertificationList
    fun set_CertificationList(value: CertificationList): Void
    fun GetILMD(): EventILMD
}
