package models.events

import interfaces.IEventKDE
import java.util.*
import java.net.URI
import java.time.OffsetDateTime

class SensorMetaData {
    var TimeStamp: OffsetDateTime? = null
    var DeviceID: URI? = null
    var DeviceMetaData: URI? = null
    var RawData: URI? = null
    var StartTime: OffsetDateTime? = null
    var BizRules: URI? = null
    var DataProcessingMethod: URI? = null
    var EndTime: OffsetDateTime? = null
    var ExtensionAttributes: List<IEventKDE> = ArrayList<IEventKDE>()

    companion object {
    }
}
