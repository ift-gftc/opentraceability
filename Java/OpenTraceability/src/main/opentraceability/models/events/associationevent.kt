package models.events

import interfaces.IEvent
import interfaces.IEventKDE
import java.util.*
import models.identifiers.*
import models.events.kdes.CertificationList
import models.events.*
import java.time.Duration
import java.lang.reflect.Type
import java.net.URI
import java.time.OffsetDateTime

class AssociationEvent /*: EventBase, IEvent*/ {

    //TODO: Review this file

    //[OpenTraceability("parentID", 7)]
    var ParentID: EPC? = null


    var Children: ArrayList<EventProduct> = ArrayList<EventProduct>()
    var Action: EventAction? = null
    var BusinessStep: URI? = null
    var Disposition: URI? = null
    var ReadPoint: EventReadPoint = EventReadPoint()
    lateinit var Location: EventLocation
    var BizTransactionList: ArrayList<EventBusinessTransaction> = ArrayList<EventBusinessTransaction>()
    var SourceList: ArrayList<EventSource> = ArrayList<EventSource>()
    var DestinationList: ArrayList<EventDestination> = ArrayList<EventDestination>()
    var SensorElementList: ArrayList<SensorElement> = ArrayList<SensorElement>()
    var PersistentDisposition: PersistentDisposition = PersistentDisposition()
    var ILMD: EventILMD = EventILMD()
    lateinit var EventType: EventType
    var Products: ArrayList<EventProduct> = ArrayList<EventProduct>()
    var EventTime: OffsetDateTime? = null
    var RecordTime: OffsetDateTime? = null
    var EventTimeZoneOffset: Duration? = null
    var EventID: URI? = null
    var ErrorDeclaration: ErrorDeclaration = ErrorDeclaration()
    var CertificationInfo: String = String()
    var CertificationList: CertificationList = CertificationList()
    var InformationProvider: PGLN = PGLN()
    var KDEs: ArrayList<IEventKDE> = ArrayList<IEventKDE>()


}
