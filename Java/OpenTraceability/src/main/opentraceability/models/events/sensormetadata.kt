package models.events
import java.util.*
import java.net.URI
import java.time.OffsetDateTime
class SensorMetaData {
    var TimeStamp: OffsetDateTime? = null
    var DeviceID: URI? = URI?()
    var DeviceMetaData: URI? = URI?()
    var RawData: URI? = URI?()
    var StartTime: OffsetDateTime? = null
    var BizRules: URI? = URI?()
    var DataProcessingMethod: URI? = URI?()
    var EndTime: OffsetDateTime? = null
    var ExtensionAttributes: List<IEventKDE> = ArrayList<IEventKDE>()
    companion object{
    }
}
