package models.events

import interfaces.IEventKDE
import utility.attributes.*
import java.util.*
import java.net.URI
import java.time.OffsetDateTime

class SensorMetaData {

    @OpenTraceabilityJsonAttribute("time")
    @OpenTraceabilityAttribute("","@time")
    var TimeStamp: OffsetDateTime? = null

    @OpenTraceabilityJsonAttribute("deviceID")
    @OpenTraceabilityAttribute("","@deviceID")
    var DeviceID: URI? = null

    @OpenTraceabilityJsonAttribute("deviceMetadata")
    @OpenTraceabilityAttribute("","@deviceMetadata")
    var DeviceMetaData: URI? = null

    @OpenTraceabilityJsonAttribute("rawData")
    @OpenTraceabilityAttribute("","@rawData")
    var RawData: URI? = null

    @OpenTraceabilityJsonAttribute("startTime")
    @OpenTraceabilityAttribute("","@startTime")
    var StartTime: OffsetDateTime? = null

    @OpenTraceabilityJsonAttribute("bizRules")
    @OpenTraceabilityAttribute("","@bizRules")
    var BizRules: URI? = null

    @OpenTraceabilityJsonAttribute("dataProcessingMethod")
    @OpenTraceabilityAttribute("","@dataProcessingMethod")
    var DataProcessingMethod: URI? = null

    @OpenTraceabilityJsonAttribute("endTime")
    @OpenTraceabilityAttribute("","@endTime")
    var EndTime: OffsetDateTime? = null

    @OpenTraceabilityExtensionAttributesAttribute
    var ExtensionAttributes: ArrayList<IEventKDE> = ArrayList<IEventKDE>()
}
