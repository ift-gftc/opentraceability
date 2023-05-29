package models.events.kdes

import models.common.Certificate
import java.util.*
import models.events.kdes.CertificationList
import utility.attributes.OpenTraceabilityArrayAttribute
import utility.attributes.OpenTraceabilityAttribute
import utility.attributes.OpenTraceabilityObjectAttribute

class CertificationList {
    @OpenTraceabilityArrayAttribute
    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","certification")
    var Certificates: ArrayList<Certificate> = ArrayList<Certificate>()
}
