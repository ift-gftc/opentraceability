package opentraceability.models.events

import opentraceability.interfaces.IEventKDE
import opentraceability.utility.attributes.*
import java.util.*
import java.net.URI
import java.time.OffsetDateTime

class SensorMetaData {

    @OpenTraceabilityJsonAttribute("time")
    @OpenTraceabilityAttribute("","@time")
    var timeStamp: OffsetDateTime? = null

    @OpenTraceabilityJsonAttribute("deviceID")
    @OpenTraceabilityAttribute("","@deviceID")
    var deviceID: URI? = null

    @OpenTraceabilityJsonAttribute("deviceMetadata")
    @OpenTraceabilityAttribute("","@deviceMetadata")
    var deviceMetaData: URI? = null

    @OpenTraceabilityJsonAttribute("rawData")
    @OpenTraceabilityAttribute("","@rawData")
    var rawData: URI? = null

    @OpenTraceabilityJsonAttribute("startTime")
    @OpenTraceabilityAttribute("","@startTime")
    var startTime: OffsetDateTime? = null

    @OpenTraceabilityJsonAttribute("bizRules")
    @OpenTraceabilityAttribute("","@bizRules")
    var bizRules: URI? = null

    @OpenTraceabilityJsonAttribute("dataProcessingMethod")
    @OpenTraceabilityAttribute("","@dataProcessingMethod")
    var dataProcessingMethod: URI? = null

    @OpenTraceabilityJsonAttribute("endTime")
    @OpenTraceabilityAttribute("","@endTime")
    var endTime: OffsetDateTime? = null

    @OpenTraceabilityExtensionAttributesAttribute
    var extensionAttributes: MutableList<IEventKDE> = mutableListOf()
}
