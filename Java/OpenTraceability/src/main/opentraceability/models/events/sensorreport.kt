package models.events

import interfaces.IEventKDE
import utility.UOM
import java.util.*
import java.lang.reflect.Type
import java.net.URI
import java.time.OffsetDateTime

class SensorReport {
    var TimeStamp: OffsetDateTime? = null
    var Type: URI? = null
    var Value: Double? = null
    var StringValue: String = String()
    var BooleanValue: Boolean? = null
    var HexBinaryValue: String = String()
    var URIValue: URI? = null
    var UOM: UOM = UOM()
    var Component: URI? = null
    var MinValue: Double? = null
    var MaxValue: Double? = null
    var SDev: Double? = null
    var ChemicalSubstance: URI? = null
    var MicroOrganism: URI? = null
    var DeviceID: URI? = null
    var RawData: URI? = null
    var MeanValue: Double? = null
    var PercentageRank: Double? = null
    var DataProcessingMethod: URI? = null
    var CoordinateReferenceSystem: URI? = null
    var Exception: URI? = null
    var PercentageValue: Double? = null
    var DeviceMetadata: URI? = null
    var ExtensionAttributes: List<IEventKDE> = ArrayList<IEventKDE>()

    companion object {
    }
}
