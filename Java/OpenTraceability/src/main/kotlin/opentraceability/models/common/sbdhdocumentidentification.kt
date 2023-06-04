package opentraceability.models.common

import opentraceability.Constants
import opentraceability.utility.attributes.OpenTraceabilityAttribute
import java.time.OffsetDateTime

class SBDHDocumentIdentification {

    @OpenTraceabilityAttribute(opentraceability.Constants.SBDH_NAMESPACE, "standard", 1)
    var Standard: String = ""

    @OpenTraceabilityAttribute(opentraceability.Constants.SBDH_NAMESPACE, "TypeVersion", 2)
    var TypeVersion: String = ""

    @OpenTraceabilityAttribute(opentraceability.Constants.SBDH_NAMESPACE, "InstanceIdentifier", 3)
    var InstanceIdentifier: String = ""


    @OpenTraceabilityAttribute(opentraceability.Constants.SBDH_NAMESPACE, "Type", 4)
    var Type: String = ""

    @OpenTraceabilityAttribute(opentraceability.Constants.SBDH_NAMESPACE, "MultipleType", 5)
    var MultipleType: String = ""


    @OpenTraceabilityAttribute(opentraceability.Constants.SBDH_NAMESPACE, "CreationDateAndTime", 6)
    var CreationDateAndTime: OffsetDateTime? = null

}
