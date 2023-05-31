package models.common

import Constants
import utility.attributes.*
import java.time.OffsetDateTime

class Certificate {

    @OpenTraceabilityJsonAttribute("gdst:certificationType")
    @OpenTraceabilityAttribute(Constants.GDST_NAMESPACE, "certificationType")
    var CertificateType: String? = null

    @OpenTraceabilityAttribute("", "certificationAgency")
    var Agency: String? = null

    @OpenTraceabilityAttribute("", "certificationStandard")
    var Standard: String? = null

    @OpenTraceabilityAttribute("","certificationValue")
    var Value: String? = null

    @OpenTraceabilityAttribute("","certificationIdentification")
    var Identification: String? = null

    @OpenTraceabilityAttribute("","certificationStartDate")
    var StartDate: OffsetDateTime? = null

    @OpenTraceabilityAttribute("","certificationEndDate")
    var EndDate: OffsetDateTime? = null

}
