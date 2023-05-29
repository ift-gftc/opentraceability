package models.common

import Constants
import utility.attributes.OpenTraceabilityAttribute
import java.lang.reflect.Type
import java.time.OffsetDateTime

class SBDHDocumentIdentification {

    @OpenTraceabilityAttribute(Constants.SBDH_NAMESPACE, "Standard", 1)
    var Standard: String = ""

    @OpenTraceabilityAttribute(Constants.SBDH_NAMESPACE, "TypeVersion", 2)
    var TypeVersion: String = ""

    @OpenTraceabilityAttribute(Constants.SBDH_NAMESPACE, "InstanceIdentifier", 3)
    var InstanceIdentifier: String = ""


    @OpenTraceabilityAttribute(Constants.SBDH_NAMESPACE, "Type", 4)
    var Type: String = ""

    @OpenTraceabilityAttribute(Constants.SBDH_NAMESPACE, "MultipleType", 5)
    var MultipleType: String = ""


    @OpenTraceabilityAttribute(Constants.SBDH_NAMESPACE, "CreationDateAndTime", 6)
    var CreationDateAndTime: OffsetDateTime? = null

}
