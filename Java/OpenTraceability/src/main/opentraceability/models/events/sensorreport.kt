package models.events

import interfaces.IEventKDE
import utility.UOM
import utility.attributes.OpenTraceabilityAttribute
import utility.attributes.OpenTraceabilityExtensionAttributesAttribute
import utility.attributes.OpenTraceabilityJsonAttribute
import java.util.*
import java.lang.reflect.Type
import java.net.URI
import java.time.OffsetDateTime

class SensorReport {

    @OpenTraceabilityJsonAttribute("time")
    @OpenTraceabilityAttribute("","@time")
    var TimeStamp: OffsetDateTime? = null

    @OpenTraceabilityJsonAttribute("type")
    @OpenTraceabilityAttribute("","@type")
    var Type: URI? = null

    @OpenTraceabilityJsonAttribute("value")
    @OpenTraceabilityAttribute("","@value")
    var Value: Double? = null

    @OpenTraceabilityJsonAttribute("stringValue")
    @OpenTraceabilityAttribute("","@stringValue")
    var StringValue: String? = null

    @OpenTraceabilityJsonAttribute("booleanValue")
    @OpenTraceabilityAttribute("","@booleanValue")
    var BooleanValue: Boolean? = null

    @OpenTraceabilityJsonAttribute("hexBinaryValue")
    @OpenTraceabilityAttribute("","@hexBinaryValue")
    var HexBinaryValue: String? = null

    @OpenTraceabilityJsonAttribute("uriValue")
    @OpenTraceabilityAttribute("","@uriValue")
    var URIValue: URI? = null

    @OpenTraceabilityJsonAttribute("uom")
    @OpenTraceabilityAttribute("","@uom")
    var UOM: UOM = UOM()

    @OpenTraceabilityJsonAttribute("component")
    @OpenTraceabilityAttribute("","@component")
    var Component: URI? = null

    @OpenTraceabilityJsonAttribute("minValue")
    @OpenTraceabilityAttribute("","@minValue")
    var MinValue: Double? = null

    @OpenTraceabilityJsonAttribute("maxValue")
    @OpenTraceabilityAttribute("","@maxValue")
    var MaxValue: Double? = null

    @OpenTraceabilityJsonAttribute("sDev")
    @OpenTraceabilityAttribute("","@sDev")
    var SDev: Double? = null

    @OpenTraceabilityJsonAttribute("chemicalSubstance")
    @OpenTraceabilityAttribute("","@chemicalSubstance")
    var ChemicalSubstance: URI? = null

    @OpenTraceabilityJsonAttribute("microorganism")
    @OpenTraceabilityAttribute("","@microorganism")
    var MicroOrganism: URI? = null

    @OpenTraceabilityJsonAttribute("deviceID")
    @OpenTraceabilityAttribute("","@deviceID")
    var DeviceID: URI? = null

    @OpenTraceabilityJsonAttribute("rawData")
    @OpenTraceabilityAttribute("","@rawData")
    var RawData: URI? = null

    @OpenTraceabilityJsonAttribute("meanValue")
    @OpenTraceabilityAttribute("","@meanValue")
    var MeanValue: Double? = null

    @OpenTraceabilityJsonAttribute("percRank")
    @OpenTraceabilityAttribute("","@percRank")
    var PercentageRank: Double? = null

    @OpenTraceabilityJsonAttribute("dataProcessingMethod")
    @OpenTraceabilityAttribute("","@dataProcessingMethod")
    var DataProcessingMethod: URI? = null

    @OpenTraceabilityJsonAttribute("coordinateReferenceSystem")
    @OpenTraceabilityAttribute("","@coordinateReferenceSystem")
    var CoordinateReferenceSystem: URI? = null

    @OpenTraceabilityJsonAttribute("exception")
    @OpenTraceabilityAttribute("","@exception")
    var Exception: URI? = null

    @OpenTraceabilityJsonAttribute("percValue")
    @OpenTraceabilityAttribute("","@percValue")
    var PercentageValue: Double? = null

    @OpenTraceabilityJsonAttribute("deviceMetadata")
    @OpenTraceabilityAttribute("","@deviceMetadata")
    var DeviceMetadata: URI? = null

    @OpenTraceabilityExtensionAttributesAttribute
    var ExtensionAttributes: ArrayList<IEventKDE> = ArrayList<IEventKDE>()
}
