package models.events

import interfaces.IEventKDE
import java.util.*
import java.net.URI
import java.time.OffsetDateTime

//TODO: review this

class SensorMetaData {

    //[OpenTraceabilityJson("time")]
    //[OpenTraceability("@time")]
    var TimeStamp: OffsetDateTime? = null

    //[OpenTraceabilityJson("deviceID")]
    //[OpenTraceability("@deviceID")]
    var DeviceID: URI? = null

    //[OpenTraceabilityJson("deviceMetadata")]
    //[OpenTraceability("@deviceMetadata")]
    var DeviceMetaData: URI? = null

    //[OpenTraceabilityJson("rawData")]
    //[OpenTraceability("@rawData")]
    var RawData: URI? = null

    //[OpenTraceabilityJson("startTime")]
    //[OpenTraceability("@startTime")]
    var StartTime: OffsetDateTime? = null

    //[OpenTraceabilityJson("bizRules")]
    //[OpenTraceability("@bizRules")]
    var BizRules: URI? = null

    //[OpenTraceabilityJson("dataProcessingMethod")]
    //[OpenTraceability("@dataProcessingMethod")]
    var DataProcessingMethod: URI? = null

    //[OpenTraceabilityJson("endTime")]
    //[OpenTraceability("@endTime")]
    var EndTime: OffsetDateTime? = null

    //[OpenTraceabilityExtensionAttributes]
    var ExtensionAttributes: List<IEventKDE> = ArrayList<IEventKDE>()
}
