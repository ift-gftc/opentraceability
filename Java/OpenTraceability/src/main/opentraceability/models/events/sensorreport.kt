package models.events
import java.util.*
import java.lang.reflect.Type
import java.net.URI
import java.time.OffsetDateTime
class SensorReport {
    var TimeStamp: OffsetDateTime? = null
    var Type: URI? = URI?()
    var Value: Double? = null
    var StringValue: String = String()
    var BooleanValue: Boolean? = null
    var HexBinaryValue: String = String()
    var URIValue: URI? = URI?()
    var UOM: UOM = UOM()
    var Component: URI? = URI?()
    var MinValue: Double? = null
    var MaxValue: Double? = null
    var SDev: Double? = null
    var ChemicalSubstance: URI? = URI?()
    var MicroOrganism: URI? = URI?()
    var DeviceID: URI? = URI?()
    var RawData: URI? = URI?()
    var MeanValue: Double? = null
    var PercentageRank: Double? = null
    var DataProcessingMethod: URI? = URI?()
    var CoordinateReferenceSystem: URI? = URI?()
    var Exception: URI? = URI?()
    var PercentageValue: Double? = null
    var DeviceMetadata: URI? = URI?()
    var ExtensionAttributes: List<IEventKDE> = ArrayList<IEventKDE>()
    companion object{
    }
}
