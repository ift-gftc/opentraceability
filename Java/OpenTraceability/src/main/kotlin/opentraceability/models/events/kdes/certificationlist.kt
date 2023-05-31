package models.events.kdes

import models.common.Certificate
import java.util.*
import utility.attributes.*

class CertificationList {
    @OpenTraceabilityArrayAttribute
    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","certification")
    var Certificates: ArrayList<Certificate> = ArrayList<Certificate>()
}
