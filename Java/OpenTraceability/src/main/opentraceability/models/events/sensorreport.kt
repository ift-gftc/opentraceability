package models.events

import interfaces.IEventKDE
import utility.UOM
import java.util.*
import java.lang.reflect.Type
import java.net.URI
import java.time.OffsetDateTime

//TODO: review this

class SensorReport {

    //[OpenTraceabilityJson("time")]
    //[OpenTraceability("@time")]
    var TimeStamp: OffsetDateTime? = null

    //[OpenTraceabilityJson("type")]
    //[OpenTraceability("@type")]
    var Type: URI? = null

    //[OpenTraceabilityJson("value")]
    //[OpenTraceability("@value")]
    var Value: Double? = null

    //[OpenTraceabilityJson("stringValue")]
    //[OpenTraceability("@stringValue")]
    var StringValue: String? = null

    //[OpenTraceabilityJson("booleanValue")]
    //[OpenTraceability("@booleanValue")]
    var BooleanValue: Boolean? = null

    //[OpenTraceabilityJson("hexBinaryValue")]
    //[OpenTraceability("@hexBinaryValue")]
    var HexBinaryValue: String? = null

    //[OpenTraceabilityJson("uriValue")]
    //[OpenTraceability("@uriValue")]
    var URIValue: URI? = null

    //[OpenTraceabilityJson("uom")]
    //[OpenTraceability("@uom")]
    var UOM: UOM = UOM()

    //[OpenTraceabilityJson("component")]
    //[OpenTraceability("@component")]
    var Component: URI? = null

    //[OpenTraceabilityJson("minValue")]
    //[OpenTraceability("@minValue")]
    var MinValue: Double? = null

    //[OpenTraceabilityJson("maxValue")]
    //[OpenTraceability("@maxValue")]
    var MaxValue: Double? = null

    //[OpenTraceabilityJson("sDev")]
    //[OpenTraceability("@sDev")]
    var SDev: Double? = null

    //[OpenTraceabilityJson("chemicalSubstance")]
    //[OpenTraceability("@chemicalSubstance")]
    var ChemicalSubstance: URI? = null

    //[OpenTraceabilityJson("microorganism")]
    //[OpenTraceability("@microorganism")]
    var MicroOrganism: URI? = null

    //[OpenTraceabilityJson("deviceID")]
    //[OpenTraceability("@deviceID")]
    var DeviceID: URI? = null

    //[OpenTraceabilityJson("rawData")]
    //[OpenTraceability("@rawData")]
    var RawData: URI? = null

    //[OpenTraceabilityJson("meanValue")]
    //[OpenTraceability("@meanValue")]
    var MeanValue: Double? = null

    //[OpenTraceabilityJson("percRank")]
    //[OpenTraceability("@percRank")]
    var PercentageRank: Double? = null

    //[OpenTraceabilityJson("dataProcessingMethod")]
    //[OpenTraceability("@dataProcessingMethod")]
    var DataProcessingMethod: URI? = null

    //[OpenTraceabilityJson("coordinateReferenceSystem")]
    //[OpenTraceability("@coordinateReferenceSystem")]
    var CoordinateReferenceSystem: URI? = null

    //[OpenTraceabilityJson("exception")]
    //[OpenTraceability("@exception")]
    var Exception: URI? = null

    //[OpenTraceabilityJson("percValue")]
    //[OpenTraceability("@percValue")]
    var PercentageValue: Double? = null

    //[OpenTraceabilityJson("deviceMetadata")]
    //[OpenTraceability("@deviceMetadata")]
    var DeviceMetadata: URI? = null

    //[OpenTraceabilityExtensionAttributes]
    var ExtensionAttributes: List<IEventKDE> = ArrayList<IEventKDE>()
}
