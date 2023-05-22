package models.common
import java.lang.reflect.Type
import java.time.OffsetDateTime
class Certificate {
    var CertificateType: String = String()
    var Agency: String = String()
    var Standard: String = String()
    var Value: String = String()
    var Identification: String = String()
    var StartDate: OffsetDateTime? = null
    var EndDate: OffsetDateTime? = null
    companion object{
    }
}
