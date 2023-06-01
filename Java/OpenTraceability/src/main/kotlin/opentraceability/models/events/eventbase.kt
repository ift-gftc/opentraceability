package models.events

import interfaces.IEventKDE
import models.events.kdes.CertificationList
import Constants
import models.events.kdes.EventKDEBoolean
import models.identifiers.PGLN
import utility.attributes.*
import java.net.URI
import java.time.*
import java.util.ArrayList

open class EventBase {
    @OpenTraceabilityAttribute("","eventTime", 1)
    var EventTime: OffsetDateTime? = null

    @OpenTraceabilityAttribute("","recordTime", 2)
    var RecordTime: OffsetDateTime? = null

    @OpenTraceabilityAttribute("","eventTimeZoneOffset", 3)
    var EventTimeZoneOffset: Duration? = null

    @OpenTraceabilityAttribute("","eventID", 4, EPCISVersion.V2)
    @OpenTraceabilityAttribute("","baseExtension/eventID", 4, EPCISVersion.V1)
    var EventID: URI? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","errorDeclaration", 5, EPCISVersion.V2)
    @OpenTraceabilityAttribute("","baseExtension/errorDeclaration", 5, EPCISVersion.V1)
    var ErrorDeclaration: ErrorDeclaration? = null

    @OpenTraceabilityAttribute("","certificationInfo", 6, EPCISVersion.V2)
    var CertificationInfo: String? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(Constants.CBVMDA_NAMESPACE, "certificationList")
    @OpenTraceabilityJsonAttribute("cbvmda:certificationList")
    var CertificationList: CertificationList? = null

    @OpenTraceabilityAttribute(Constants.CBVMDA_NAMESPACE, "informationProvider")
    @OpenTraceabilityJsonAttribute("cbvmda:informationProvider")
    var InformationProvider: PGLN? = null

    @OpenTraceabilityExtensionElementsAttribute
    var KDEs: MutableList<IEventKDE> = mutableListOf()

    open fun <T: IEventKDE> GetKDE(clazz: Class<T>, ns: String, name: String): T? {
        val kde = KDEs.find { it.Namespace == ns && it.Name == name }
        if (kde != null) {
            if (clazz.isInstance(kde))
            {
                @Suppress("UNCHECKED_CAST")
                return kde as T
            }
        }

        return null
    }

    open fun <T: IEventKDE> GetKDE(clazz: Class<T>): T? {
        val kde = KDEs.find { clazz.isInstance(it) }
        if (kde != null) {
            @Suppress("UNCHECKED_CAST")
            return kde as T
        }
        return null
    }
}
