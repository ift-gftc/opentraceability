package models.events

import interfaces.IEventKDE
import java.util.*
import models.identifiers.*
import models.events.kdes.CertificationList
import java.time.Duration
import java.net.URI
import java.time.OffsetDateTime

open class EventBase {

    //TODO: review this file

    //[OpenTraceability("eventTime", 1)]
    var EventTime: OffsetDateTime? = null

    //[OpenTraceability("recordTime", 2)]
    var RecordTime: OffsetDateTime? = null

    //[OpenTraceability("eventTimeZoneOffset", 3)]
    var EventTimeZoneOffset: Duration? = null

    //[OpenTraceability("eventID", 4, EPCISVersion.V2)]
    //[OpenTraceability("baseExtension/eventID", 4, EPCISVersion.V1)]
    var EventID: URI? = null

    //[OpenTraceabilityObject]
    //[OpenTraceability("errorDeclaration", 5, EPCISVersion.V2)]
    //[OpenTraceability("baseExtension/errorDeclaration", 5, EPCISVersion.V1)]
    var ErrorDeclaration: ErrorDeclaration = ErrorDeclaration()

    //[OpenTraceability("certificationInfo", 6, EPCISVersion.V2)]
    var CertificationInfo: String = String()

    //[OpenTraceabilityObject]
    //[OpenTraceability(Constants.CBVMDA_NAMESPACE, "certificationList")]
    //[OpenTraceabilityJson("cbvmda:certificationList")]
    var CertificationList: CertificationList = CertificationList()

    //[OpenTraceability(Constants.CBVMDA_NAMESPACE, "informationProvider")]
    //[OpenTraceabilityJson("cbvmda:informationProvider")]
    var InformationProvider: PGLN = PGLN()

    //[OpenTraceabilityExtensionElements]
    var KDEs: List<IEventKDE> = ArrayList<IEventKDE>()



    fun <T> GetKDE(ns: String, name: String) : T?{
        TODO("Not yet implemented")
    }

    fun <T> GetKDE() : T?{
        TODO("Not yet implemented")
    }
}
