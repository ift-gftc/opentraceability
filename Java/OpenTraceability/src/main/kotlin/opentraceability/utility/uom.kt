package opentraceability.utility

import org.json.JSONObject
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class UOM {
    val _locker = Any()
    var Name: String = ""
    var Abbreviation: String = ""
    var UnitDimension: String = ""
    var SubGroup: String = ""
    var UNCode: String = ""
    var A: Double = 0.0
    var B: Double = 0.0
    var C: Double = 0.0
    var D: Double = 0.0
    var Offset: Double = 0.0

    constructor(){}

    constructor(uom: UOM) {
        this.Abbreviation = uom.Abbreviation
        this.Name = uom.Name
        this.UnitDimension= uom.UnitDimension
        this.UNCode= uom.UNCode
        this.SubGroup = uom.SubGroup
        this.Offset = uom.Offset
        this.A = uom.A
        this.B = uom.B
        this.C = uom.C
        this.D= uom.D
    }


    constructor(juom: JSONObject) {
        this.A = 0.0
        this.B = 1.0
        this.C = 1.0
        this.D = 0.0

        this.Name = juom["name"]?.toString() ?: throw Exception("name not set on uom json. $juom")
        this.UNCode = juom["UNCode"]?.toString() ?: throw Exception("UNCode not set on uom json. $juom")
        this.Abbreviation = juom["symbol"]?.toString() ?: throw Exception("symbol not set on uom json. $juom")
        this.UnitDimension = juom["type"]?.toString() ?: throw Exception("type not set on uom json. $juom")

        this.Offset = juom["offset"]?.toString()!!.toDouble()

        val multiplierString = juom["multiplier"]?.toString() ?: throw Exception("multiplier not set on uom json. $juom")
        val (B, C) = if (multiplierString.contains("/")) {
            val numerator = multiplierString.split('/').first().toInt()
            val denominator = multiplierString.split('/').last().toInt()
            numerator.toDouble() to denominator.toDouble()
        } else {
            val multiplier = juom["multiplier"]?.toString() ?: throw Exception("multiplier not set on uom json. $juom")
            multiplier to 1.0
        }

    }


    companion object {
        private val uomListLock = ReentrantLock()
        private var uomList: MutableList<UOM>? = null

        fun lookUpFromUNCode(unCode: String): UOM {
            return uomListLock.withLock {
                val uom = getUOMList().find { it.UNCode == unCode }
                return uom ?: UOM()
            }
        }

        fun isNullOrEmpty(uom: UOM?): Boolean {
            return uom == null || uom.Abbreviation.isNullOrEmpty()
        }

        fun parseFromName(name: String): UOM {
            try {
                if (name.isNullOrEmpty()) {
                    throw IllegalArgumentException("name")
                }

                val uom = getUOMList().find { it.Name == name }
                    ?: getUOMList().find { it.UNCode.equals(name, ignoreCase = true) }
                    ?: throw Exception("Failed to parse UOM")

                return UOM(uom)
            } catch (ex: Exception) {
                opentraceability.OTLogger.error(ex)
                throw ex
            }
        }

        fun getUOMList(): MutableList<UOM> {
            if (uomList == null) {
                // Initialize the list if it's null
                uomList = loadUOMList()
            }
            return uomList!!
        }

        fun loadUOMList(): MutableList<UOM> {
            // Perform the loading of UOMs from the appropriate source
            // and return the list of UOM objects
            // ...
            return mutableListOf()
        }
    }


    fun CopyFrom(uom: UOM) {
        this.Abbreviation = uom.Abbreviation
        this.Name = uom.Name
        this.UnitDimension= uom.UnitDimension
        this.UNCode= uom.UNCode
        this.SubGroup = uom.SubGroup
        this.Offset = uom.Offset
        this.A = uom.A
        this.B = uom.B
        this.C = uom.C
        this.D= uom.D
    }


    fun isBase(): Boolean {
        return A == 0.0 && B == 1.0 && C == 1.0 && D == 0.0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is UOM) {
            return false
        }
        return UNCode == other.UNCode
    }

    override fun hashCode(): Int {
        return UNCode.hashCode()
    }

    val key: String
        get() = Abbreviation

    fun convert(value: Double, to: UOM): Double {
        val valueBase = this.toBase(value)
        val valueNew = to.fromBase(valueBase)
        return valueNew
    }

    fun toBase(value: Double): Double {
        val baseValue = ((A + B * value) / (C + D * value)) - Offset
        return baseValue
    }

    fun fromBase(baseValue: Double): Double {
        val value = ((A - C * baseValue) / (D * baseValue - B)) + Offset
        return value
    }

    override fun toString(): String {
        return "$Name [$Abbreviation]"
    }
}
