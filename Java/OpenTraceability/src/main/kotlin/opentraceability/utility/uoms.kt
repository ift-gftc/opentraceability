package opentraceability.utility

import opentraceability.OTLogger
import java.util.*
import org.json.*

object UOMS {
    private val uomsAbbrevDict = mutableMapOf<String, UOM>()
    private val uomsUNCodeDict = mutableMapOf<String, UOM>()

    init {
        load()
    }

    fun load() {
        try {
            // Load the subscriptions JSON
            val loader = EmbeddedResourceLoader()
            val jsonText = loader.readString("OpenTraceability", "OpenTraceability.Utility.Data.uoms.json")
            val jarr = JSONArray(jsonText)

            for (i in 0 until jarr.length()) {
                val juom = jarr.getJSONObject(i)
                val uom = UOM(juom)
                val lowerCaseAbbreviation = uom.Abbreviation.toLowerCase()
                val upperCaseUNCode = uom.UNCode.toUpperCase()

                if (!uomsAbbrevDict.containsKey(lowerCaseAbbreviation)) {
                    uomsAbbrevDict[lowerCaseAbbreviation] = uom
                } else {
                    println("Duplicate Unit abbreviation detected: ${uom.Abbreviation}")
                }

                if (!uomsUNCodeDict.containsKey(upperCaseUNCode)) {
                    uomsUNCodeDict[upperCaseUNCode] = uom
                } else {
                    println("Duplicate Unit UNCode detected: ${uom.UNCode}")
                }
            }
        } catch (ex: Exception) {
            opentraceability.OTLogger.error(ex)
            throw ex
        }
    }


    fun getBase(uom: UOM): UOM {
        return getBase(uom.UnitDimension)
    }

    fun getBase(dimension: String): UOM {
        for (entry in uomsAbbrevDict) {
            if (entry.value.UnitDimension == dimension && entry.value.isBase()) {
                return entry.value
            }
        }
        throw Exception("Failed to get base for dimension = $dimension")
    }

    fun getUOMFromName(name: String): UOM? {
        var uom: UOM? = null
        var formattedName = name.toLowerCase()
        if (formattedName == "count") {
            formattedName = "ea"
        }
        if (formattedName == "pound" || formattedName == "pounds" || formattedName == "ib") {
            formattedName = "lb"
        }
        if (formattedName[formattedName.length - 1] == '.') {
            formattedName = formattedName.substring(0, formattedName.length - 1)
        }
        if (uomsAbbrevDict.containsKey(formattedName)) {
            uom = uomsAbbrevDict[formattedName]
        } else {
            for (entry in uomsAbbrevDict) {
                if (entry.value.Name.toLowerCase() == formattedName) {
                    uom = entry.value
                    break
                }
            }
            if (uom == null) {
                if (formattedName[formattedName.length - 1] == 's') {
                    formattedName = formattedName.substring(0, formattedName.length - 1)
                    for (entry in uomsAbbrevDict) {
                        if (entry.value.Name.toLowerCase() == formattedName) {
                            uom = entry.value
                            break
                        }
                    }
                }
            }
        }
        return uom
    }

    fun getUOMFromUNCode(name: String): UOM? {
        val formattedName = name.toUpperCase()
        return uomsUNCodeDict[formattedName] ?: uomsAbbrevDict.values.find { it.UNCode.equals(formattedName, ignoreCase = true) }
    }

    val list: MutableList<UOM>
        get() = uomsAbbrevDict.values.toMutableList()
}
