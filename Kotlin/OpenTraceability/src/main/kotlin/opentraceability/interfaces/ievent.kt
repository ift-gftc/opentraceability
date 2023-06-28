package opentraceability.interfaces

import opentraceability.models.events.kdes.CertificationList
import opentraceability.models.events.*
import java.time.*
import java.net.URI

interface IEvent {

    var eventID: URI?
    var certificationInfo: String?
    var eventTime: OffsetDateTime?
    var eventTimeZoneOffset: Duration?
    var recordTime: OffsetDateTime?
    var eventType: EventType
    var action: EventAction?
    var businessStep: URI?
    var disposition: URI?
    var persistentDisposition: PersistentDisposition?
    var errorDeclaration: ErrorDeclaration?
    var location: EventLocation?
    var readPoint: EventReadPoint?
    var bizTransactionList: MutableList<EventBusinessTransaction>
    var sourceList: MutableList<EventSource>
    var destinationList: MutableList<EventDestination>
    var kdes: MutableList<IEventKDE>
    var sensorElementList: MutableList<SensorElement>
    var products: MutableList<EventProduct>
    var certificationList: CertificationList?

    fun <T: IEventKDE> getKDE(clazz: Class<T>, ns: String, name: String): T?
    fun <T: IEventKDE> getKDE(clazz: Class<T>): T?
    fun addProduct(product: EventProduct)
    fun grabILMD(): EventILMD?
}
