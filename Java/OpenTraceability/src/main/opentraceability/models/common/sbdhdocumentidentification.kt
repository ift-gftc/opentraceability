package models.common

import java.lang.reflect.Type
import java.time.OffsetDateTime

class SBDHDocumentIdentification {

    //TODO: check this attributes

    // [OpenTraceability(Constants.SBDH_NAMESPACE, "Standard", 1)]
    var Standard: String = ""

    //[OpenTraceability(Constants.SBDH_NAMESPACE, "TypeVersion", 2)]
    var TypeVersion: String = ""

    //[OpenTraceability(Constants.SBDH_NAMESPACE, "InstanceIdentifier", 3)]
    var InstanceIdentifier: String = ""


    //[OpenTraceability(Constants.SBDH_NAMESPACE, "Type", 4)]
    var Type: String = ""

    //[OpenTraceability(Constants.SBDH_NAMESPACE, "MultipleType", 5)]
    var MultipleType: String = ""


    //[OpenTraceability(Constants.SBDH_NAMESPACE, "CreationDateAndTime", 6)]
    var CreationDateAndTime: OffsetDateTime? = null

}
