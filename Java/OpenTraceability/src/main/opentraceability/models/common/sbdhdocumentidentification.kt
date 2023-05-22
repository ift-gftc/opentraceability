package models.common
import java.lang.reflect.Type
import java.time.OffsetDateTime
class SBDHDocumentIdentification {
    var Standard: String = ""
    var TypeVersion: String = ""
    var InstanceIdentifier: String = ""
    var Type: String = ""
    var MultipleType: String = ""
    var CreationDateAndTime: OffsetDateTime? = null
    companion object{
    }
}
