package opentraceability.models.events

import opentraceability.interfaces.IEventKDE
import opentraceability.utility.UOM
import opentraceability.utility.attributes.*
import java.util.*
import java.net.URI
import java.time.OffsetDateTime

class SensorReport {

    @OpenTraceabilityJsonAttribute("time")
    @OpenTraceabilityAttribute("","@time")
    var timeStamp: OffsetDateTime? = null

    @OpenTraceabilityJsonAttribute("type")
    @OpenTraceabilityAttribute("","@type")
    var type: URI? = null

    @OpenTraceabilityJsonAttribute("value")
    @OpenTraceabilityAttribute("","@value")
    var value: Double? = null

    @OpenTraceabilityJsonAttribute("stringValue")
    @OpenTraceabilityAttribute("","@stringValue")
    var stringValue: String? = null

    @OpenTraceabilityJsonAttribute("booleanValue")
    @OpenTraceabilityAttribute("","@booleanValue")
    var booleanValue: Boolean? = null

    @OpenTraceabilityJsonAttribute("hexBinaryValue")
    @OpenTraceabilityAttribute("","@hexBinaryValue")
    var hexBinaryValue: String? = null

    @OpenTraceabilityJsonAttribute("uriValue")
    @OpenTraceabilityAttribute("","@uriValue")
    var uriValue: URI? = null

    @OpenTraceabilityJsonAttribute("uom")
    @OpenTraceabilityAttribute("","@uom")
    var uom: UOM = UOM()

    @OpenTraceabilityJsonAttribute("component")
    @OpenTraceabilityAttribute("","@component")
    var component: URI? = null

    @OpenTraceabilityJsonAttribute("minValue")
    @OpenTraceabilityAttribute("","@minValue")
    var minValue: Double? = null

    @OpenTraceabilityJsonAttribute("maxValue")
    @OpenTraceabilityAttribute("","@maxValue")
    var maxValue: Double? = null

    @OpenTraceabilityJsonAttribute("sDev")
    @OpenTraceabilityAttribute("","@sDev")
    var sDev: Double? = null

    @OpenTraceabilityJsonAttribute("chemicalSubstance")
    @OpenTraceabilityAttribute("","@chemicalSubstance")
    var chemicalSubstance: URI? = null

    @OpenTraceabilityJsonAttribute("microorganism")
    @OpenTraceabilityAttribute("","@microorganism")
    var microOrganism: URI? = null

    @OpenTraceabilityJsonAttribute("deviceID")
    @OpenTraceabilityAttribute("","@deviceID")
    var deviceID: URI? = null

    @OpenTraceabilityJsonAttribute("rawData")
    @OpenTraceabilityAttribute("","@rawData")
    var rawData: URI? = null

    @OpenTraceabilityJsonAttribute("meanValue")
    @OpenTraceabilityAttribute("","@meanValue")
    var meanValue: Double? = null

    @OpenTraceabilityJsonAttribute("percRank")
    @OpenTraceabilityAttribute("","@percRank")
    var percentageRank: Double? = null

    @OpenTraceabilityJsonAttribute("dataProcessingMethod")
    @OpenTraceabilityAttribute("","@dataProcessingMethod")
    var dataProcessingMethod: URI? = null

    @OpenTraceabilityJsonAttribute("coordinateReferenceSystem")
    @OpenTraceabilityAttribute("","@coordinateReferenceSystem")
    var coordinateReferenceSystem: URI? = null

    @OpenTraceabilityJsonAttribute("exception")
    @OpenTraceabilityAttribute("","@exception")
    var exception: URI? = null

    @OpenTraceabilityJsonAttribute("percValue")
    @OpenTraceabilityAttribute("","@percValue")
    var percentageValue: Double? = null

    @OpenTraceabilityJsonAttribute("deviceMetadata")
    @OpenTraceabilityAttribute("","@deviceMetadata")
    var deviceMetadata: URI? = null

    @OpenTraceabilityExtensionAttributesAttribute
    var extensionAttributes: MutableList<IEventKDE> = mutableListOf()
}
