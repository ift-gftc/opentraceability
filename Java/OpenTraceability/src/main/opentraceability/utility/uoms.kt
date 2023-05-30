package utility

import OTLogger
import org.intellij.markdown.lexer.push
import java.util.*

import org.json.JSONArray

object UOMS {
    private val uomsAbbrevDict = mutableMapOf<String, UOM>()
    private val uomsUNCodeDict = mutableMapOf<String, UOM>()

    init {
        load()
    }

    fun load() {
        try {
            val loader = EmbeddedResourceLoader()
            val jarr = JSONArray(loader.readString("OpenTraceability", "OpenTraceability.Utility.Data.uoms.json"))
            for (i in 0 until jarr.length()) {
                val juom = jarr.getJSONObject(i)
                val uom = UOM(juom)
                if (!uomsAbbrevDict.containsKey(uom.abbreviation.toLowerCase())) {
                    uomsAbbrevDict[uom.abbreviation.toLowerCase()] = uom
                } else {
                    println("Duplicate Unit abbreviation detected: ${uom.abbreviation}")
                }
                if (!uomsUNCodeDict.containsKey(uom.unCode.toUpperCase())) {
                    uomsUNCodeDict[uom.unCode.toUpperCase()] = uom
                } else {
                    println("Duplicate Unit UNCode detected: ${uom.unCode}")
                }
            }
        } catch (ex: Exception) {
            OTLogger.error(ex)
            throw ex
        }
    }

    fun getBase(uom: UOM): UOM {
        return getBase(uom.unitDimension)
    }

    fun getBase(dimension: String): UOM {
        for (entry in uomsAbbrevDict) {
            if (entry.value.unitDimension == dimension && entry.value.isBase()) {
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
                if (entry.value.name.toLowerCase() == formattedName) {
                    uom = entry.value
                    break
                }
            }
            if (uom == null) {
                if (formattedName[formattedName.length - 1] == 's') {
                    formattedName = formattedName.substring(0, formattedName.length - 1)
                    for (entry in uomsAbbrevDict) {
                        if (entry.value.name.toLowerCase() == formattedName) {
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
        return uomsUNCodeDict[formattedName] ?: uomsAbbrevDict.values.find { it.unCode.equals(formattedName, ignoreCase = true) }
    }

    val list: List<UOM>
        get() = uomsAbbrevDict.values.toList()
}
