package opentraceability.models.events.kdes

import opentraceability.models.common.Certificate
import opentraceability.utility.attributes.*
import java.util.*

class CertificationList {
    @OpenTraceabilityArrayAttribute
    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","certification")
    var certificates: MutableList<Certificate> = mutableListOf()
}
