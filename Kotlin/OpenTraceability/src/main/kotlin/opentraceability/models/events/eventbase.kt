package opentraceability.models.events

import opentraceability.interfaces.IEventKDE
import opentraceability.models.events.kdes.CertificationList
import opentraceability.Constants
import opentraceability.models.identifiers.PGLN
import opentraceability.utility.attributes.*
import java.net.URI
import java.time.*

open class EventBase {
    @OpenTraceabilityAttribute("","eventTime", 1)
    var eventTime: OffsetDateTime? = null

    @OpenTraceabilityAttribute("","recordTime", 2)
    var recordTime: OffsetDateTime? = null

    @OpenTraceabilityAttribute("","eventTimeZoneOffset", 3)
    var eventTimeZoneOffset: Duration? = null

    @OpenTraceabilityAttribute("","eventID", 4, EPCISVersion.V2)
    @OpenTraceabilityAttribute("","baseExtension/eventID", 4, EPCISVersion.V1)
    var eventID: URI? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","errorDeclaration", 5, EPCISVersion.V2)
    @OpenTraceabilityAttribute("","baseExtension/errorDeclaration", 5, EPCISVersion.V1)
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

    open fun <T: IEventKDE> getKDE(clazz: Class<T>, ns: String, name: String): T? {
        val kde = kdes.find { it.namespace == ns && it.name == name }
        if (kde != null) {
            if (clazz.isInstance(kde))
            {
                @Suppress("UNCHECKED_CAST")
                return kde as T
            }
        }

        return null
    }

    open fun <T: IEventKDE> getKDE(clazz: Class<T>): T? {
        val kde = kdes.find { clazz.isInstance(it) }
        if (kde != null) {
            @Suppress("UNCHECKED_CAST")
            return kde as T
        }
        return null
    }
}
