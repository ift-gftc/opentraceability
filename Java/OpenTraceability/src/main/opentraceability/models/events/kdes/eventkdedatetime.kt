package models.events.kdes
import java.lang.reflect.Type
import java.time.OffsetDateTime
class EventKDEDateTime {
    var ValueType: Type = Type()
    var Value: OffsetDateTime? = null
    var Namespace: String = ""
    var Name: String = ""
    companion object{
    }
}
