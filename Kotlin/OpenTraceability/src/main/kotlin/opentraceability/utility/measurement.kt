package opentraceability.utility

import java.lang.Exception
import org.w3c.dom.Element
import kotlin.math.round

class Measurement : Comparable<Measurement?> {
    var value: Double = 0.0
    var uom: UOM = UOM()


    constructor() {}

    constructor(xmlElement: Element) {
        value = xmlElement.getAttribute("Value")?.toDoubleOrNull() ?: 0.0
        uom = UOM.parseFromName(xmlElement.getAttribute("UoM") ?: "")
    }

    constructor(copyFrom: Measurement) {
        value = copyFrom.value
        uom = UOM(copyFrom.uom)
    }

    constructor(value: Double, unitCode: UOM){
        this.value = value
        uom = unitCode
    }

    constructor(value: Double, unitCode: String)  {
        this.value = value
        uom = UOM.parseFromName(unitCode)
    }

    fun add(measurement: Measurement) {
        try {
            measurement?.let {
                value += it.value
            }
        } catch (ex: Exception) {
            opentraceability.OTLogger.error(ex)
            throw ex
        }
    }

    operator fun plus(right: Measurement): Measurement {
        if (uom.UnitDimension != right.uom.UnitDimension) {
            throw Exception("All operands must be of the same unit dimension. Left UoM = ${uom.UNCode} | Right UoM = ${right.uom.UNCode}.")
        }

        val rightValue = right.uom.convert(right.value, uom)
        val sum = value + rightValue

        return Measurement(sum, uom)
    }

    operator fun minus(right: Measurement): Measurement {
        if (uom.UnitDimension != right.uom.UnitDimension) {
            throw Exception("All operands must be of the same unit dimension.")
        }

        val rightValue = right.uom.convert(right.value, uom)
        val diff = value - rightValue
        return Measurement(diff, uom)
    }

    operator fun times(factor: Double): Measurement {
        val newValue = value * factor
        return Measurement(newValue, uom)
    }

    fun toBase(): Measurement {
        if (uom.UNCode.isNullOrBlank()) return this

        val trBase = Measurement()
        trBase.uom = UOMS.getBase(uom)

        if (trBase.uom.UNCode.isNullOrBlank()) throw NullPointerException("Failed to look up base UoM. UNCode=${uom.UNCode}")

        trBase.value = uom.convert(value, trBase.uom)
        trBase.value = round(trBase.value)
        return trBase
    }

    fun convertTo(uomStr: String): Measurement {
        if (uomStr.isNullOrEmpty()) {
            return this
        }
        val trBase = Measurement()
        trBase.uom = UOMS.getUOMFromUNCode(uomStr)!!
        if (trBase.uom == null) {
            return this
        }
        trBase.value = uom.convert(value, trBase.uom)
        trBase.value = round(trBase.value)
        return trBase
    }

    fun convertTo(uom: UOM): Measurement {
        if (uom == null) {
            return this
        }
        val trBase = Measurement()
        trBase.uom = uom
        trBase.value = uom.convert(value, trBase.uom)
        trBase.value = round(trBase.value)
        return trBase
    }

    override fun toString(): String {
        try {
            var str = value.toString()
            str += " " + uom.UNCode
            return str
        } catch (ex: Exception) {
            opentraceability.OTLogger.error(ex)
            throw ex
        }
    }

    fun toStringEx(): String {
        try {
            var str = value.toString()
            str += " " + uom.Abbreviation
            return str
        } catch (ex: Exception) {
            opentraceability.OTLogger.error(ex)
            throw ex
        }
    }

    fun getUniquenessKey(iVersion: Int): String {
        try {
            return if (value == null && uom == null) {
                ""
            } else {
                toBase().toString().trim()
            }
        } catch (ex: Exception) {
            opentraceability.OTLogger.error(ex)
            throw ex
        }
    }

    companion object {
        fun parse(strValue: String): Measurement {
            try {
                if (strValue.isNullOrBlank()) {
                    val emptyMeasurement = Measurement()
                    return emptyMeasurement
                }

                var numberStr = ""
                var uomStr = ""

                val strParts = strValue.split(" ")
                if (strParts.size != 2) {
                    throw Exception("Invalid Measurment string encountered, value=$strValue. String must have a value and the UOM UN Code.")
                }
                numberStr = strParts[0].trim()
                uomStr = strParts[1].trim()

                val uoms = UOMS.list

                val dblValue = numberStr.toDouble()
                var uom = UOMS.getUOMFromUNCode(uomStr)
                if (uom == null) {
                    uom = uoms.find { it.Abbreviation.toLowerCase() == uomStr.toLowerCase() || uomStr.toLowerCase() == it.Name.toLowerCase() }
                }

                if (uom == null) {
                    throw Exception("Failed to recognize UoM while parsing a TRMeasurement from a string. String=$strValue, Value=$numberStr, UoM=$uomStr")
                }

                val measurement = Measurement()
                measurement.value = dblValue
                measurement.uom = uom
                return measurement
            } catch (ex: Exception) {
                opentraceability.OTLogger.error(ex)
                throw ex
            }
        }

        fun TryParse(strValue: String): Measurement? {
            var measure: Measurement? = null
            try {
                if (!strValue.isNullOrEmpty()) {
                    measure = parse(strValue)
                }
            } catch (ex: Exception) {

            }
            return measure
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other != null && other is Measurement) {
            val otherMeasurement = other as Measurement
            if (value == otherMeasurement.value && uom == otherMeasurement.uom) {
                return true
            }
        }
        return false
    }

    override fun compareTo(other: Measurement?): Int {
        if (other == null) {
            return 1
        }

        val thisBase = toBase()
        val otherBase = other.toBase()
        return when {
            thisBase.value == otherBase.value -> 0
            thisBase.value < otherBase.value -> -1
            else -> 1
        }
    }

    override fun hashCode(): Int {
        return value.hashCode() + (uom?.hashCode() ?: 0)
    }
}

