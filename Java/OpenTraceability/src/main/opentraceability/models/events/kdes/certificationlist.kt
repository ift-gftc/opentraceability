package opentraceability.models.events.kdes

import opentraceability.models.common.Certificate
import java.util.*
import opentraceability.models.events.kdes.CertificationList
import opentraceability.utility.attributes.OpenTraceabilityArrayAttribute
import opentraceability.utility.attributes.OpenTraceabilityAttribute
import opentraceability.utility.attributes.OpenTraceabilityObjectAttribute

class CertificationList {
    @OpenTraceabilityArrayAttribute
    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","certification")
    var Certificates: ArrayList<Certificate> = ArrayList<Certificate>()
}
