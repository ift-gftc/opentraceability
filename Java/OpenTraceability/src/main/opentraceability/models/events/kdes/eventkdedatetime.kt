package models.events.kdes

import java.lang.reflect.Type
import java.time.OffsetDateTime

class EventKDEDateTime {
    companion object {
    }

    var ValueType: Type = Type()
    var Value: OffsetDateTime? = null
    var Namespace: String = ""
    var Name: String = ""

}
