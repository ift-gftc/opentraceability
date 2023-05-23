package models.common

import java.time.OffsetDateTime

class Certificate {

    //TODO: check this attributes

    //[OpenTraceabilityJson("gdst:certificationType")]
    //[OpenTraceability(Constants.GDST_NAMESPACE, "certificationType")]
    var CertificateType: String? = null

    //[OpenTraceability("certificationAgency")]
    var Agency: String? = null

    //[OpenTraceability("certificationStandard")]
    var Standard: String? = null

    //[OpenTraceability("certificationValue")]
    var Value: String? = null

    //[OpenTraceability("certificationIdentification")]
    var Identification: String? = null

    //[OpenTraceability("certificationStartDate")]
    var StartDate: OffsetDateTime? = null

    //[OpenTraceability("certificationEndDate")]
    var EndDate: OffsetDateTime? = null

}
