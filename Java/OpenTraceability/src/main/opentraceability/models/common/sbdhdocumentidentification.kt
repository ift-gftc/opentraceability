package models.common

import java.lang.reflect.Type
import java.time.OffsetDateTime

class SBDHDocumentIdentification {
    companion object {
    }

    var Standard: String = ""
    var TypeVersion: String = ""
    var InstanceIdentifier: String = ""
    var Type: String = ""
    var MultipleType: String = ""
    var CreationDateAndTime: OffsetDateTime? = null

}
