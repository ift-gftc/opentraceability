package opentraceability.models.events

import opentraceability.interfaces.IEventKDE
import opentraceability.models.events.kdes.CertificationList
import opentraceability.Constants
import opentraceability.utility.attributes.*
import opentraceability.utility.Country
import java.util.*
import java.time.OffsetDateTime

class EventILMD {
    @OpenTraceabilityAttribute(Constants.CBVMDA_NAMESPACE, "productionMethodForFishAndSeafoodCode")
    @OpenTraceabilityJsonAttribute("cbvmda:productionMethodForFishAndSeafoodCode")
    var productionMethodForFishAndSeafoodCode: String? = null

    @OpenTraceabilityAttribute(Constants.CBVMDA_NAMESPACE, "itemExpirationDate")
    @OpenTraceabilityJsonAttribute("cbvmda:itemExpirationDate")
    var itemExpirationDate: OffsetDateTime? = null

    @OpenTraceabilityAttribute(Constants.CBVMDA_NAMESPACE, "productionDate")
    @OpenTraceabilityJsonAttribute("cbvmda:productionDate")
    var productionDate: OffsetDateTime? = null

    @OpenTraceabilityArrayAttribute
    @OpenTraceabilityRepeatingAttribute
    @OpenTraceabilityAttribute(Constants.CBVMDA_NAMESPACE, "countryOfOrigin")
    @OpenTraceabilityJsonAttribute("cbvmda:countryOfOrigin")
    var countryOfOrigin: MutableList<Country> = mutableListOf()

    @OpenTraceabilityAttribute(Constants.CBVMDA_NAMESPACE, "lotNumber")
    @OpenTraceabilityJsonAttribute("cbvmda:lotNumber")
    var lotNumber: String? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(Constants.CBVMDA_NAMESPACE, "certificationList")
    @OpenTraceabilityJsonAttribute("cbvmda:certificationList")
    var certificationList: CertificationList? = null

    @OpenTraceabilityExtensionElementsAttribute
    var extensionKDEs: MutableList<IEventKDE> = mutableListOf()
}
