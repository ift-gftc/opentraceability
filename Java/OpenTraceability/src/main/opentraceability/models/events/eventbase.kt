package models.events

import interfaces.IEventKDE
import models.events.kdes.CertificationList
import Constants
import models.identifiers.PGLN
import utility.attributes.OpenTraceabilityAttribute
import utility.attributes.OpenTraceabilityExtensionElementsAttribute
import utility.attributes.OpenTraceabilityJsonAttribute
import utility.attributes.OpenTraceabilityObjectAttribute
import java.net.URI
import java.time.Duration
import java.time.OffsetDateTime

open class EventBase {
    @OpenTraceabilityAttribute("","eventTime", 1)
    var eventTime: OffsetDateTime? = null

    @OpenTraceabilityAttribute("","recordTime", 2)
    var recordTime: OffsetDateTime? = null

    @OpenTraceabilityAttribute("","eventTimeZoneOffset", 3)
    var eventTimeZoneOffset: Duration? = null

    @OpenTraceabilityAttribute("","eventID", 4, EPCISVersion.V2)
    //@OpenTraceabilityAttribute("","baseExtension/eventID", 4, EPCISVersion.V1)
    var eventID: URI? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","errorDeclaration", 5, EPCISVersion.V2)
    //@OpenTraceabilityAttribute("","baseExtension/errorDeclaration", 5, EPCISVersion.V1)
    var errorDeclaration: ErrorDeclaration? = null

    @OpenTraceabilityAttribute("","certificationInfo", 6, EPCISVersion.V2)
    var certificationInfo: String? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(Constants.CBVMDA_NAMESPACE, "certificationList")
    @OpenTraceabilityJsonAttribute("cbvmda:certificationList")
    var certificationList: CertificationList? = null

    @OpenTraceabilityAttribute(Constants.CBVMDA_NAMESPACE, "informationProvider")
    @OpenTraceabilityJsonAttribute("cbvmda:informationProvider")
    var informationProvider: PGLN? = null

    @OpenTraceabilityExtensionElementsAttribute
    var kdes: MutableList<IEventKDE> = mutableListOf()

    inline fun <reified T: IEventKDE> getKDE(ns: String, name: String): T? {
        val kde = kdes.find { it.Namespace == ns && it.Name == name }
        if (kde != null) {
            if (kde is T) {
                return kde
            }
        }
        return null
    }

    inline fun <reified T: IEventKDE> getKDE(): T? {
        val kde = kdes.find { it.ValueType == T::class.java }
        if (kde != null) {
            if (kde is T) {
                return kde
            }
        }
        return null
    }
}
