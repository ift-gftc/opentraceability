package opentraceability.models.common

import opentraceability.Constants
import opentraceability.utility.attributes.*
import java.time.OffsetDateTime

class Certificate {

    @OpenTraceabilityJsonAttribute("gdst:certificationType")
    @OpenTraceabilityAttribute(Constants.GDST_NAMESPACE, "certificationType")
    var certificateType: String? = null

    @OpenTraceabilityAttribute("", "certificationAgency")
    var agency: String? = null

    @OpenTraceabilityAttribute("", "certificationStandard")
    var standard: String? = null

    @OpenTraceabilityAttribute("","certificationValue")
    var value: String? = null

    @OpenTraceabilityAttribute("","certificationIdentification")
    var identification: String? = null

    @OpenTraceabilityAttribute("","certificationStartDate")
    var startDate: OffsetDateTime? = null

    @OpenTraceabilityAttribute("","certificationEndDate")
    var endDate: OffsetDateTime? = null

}
