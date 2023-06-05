package opentraceability.models.identifiers

import opentraceability.utility.*
import opentraceability.utility.StringExtensions.IsURICompatibleChars
import opentraceability.OTLogger
import java.net.URI

//@JsonDeserialize(using = EPCDeserializer::class)
//@JsonSerialize(using = EPCSerializer::class)
class EPC {

    internal var _epcStr: String = ""

    lateinit var Type: EPCType
    var GTIN: GTIN? = null
    var SerialLotNumber: String? = null


    constructor(epcStr: String?) {
        try {
            val error = EPC.DetectEPCIssue(epcStr)

            if (!error.isNullOrBlank()) {
                throw Exception("The EPC $epcStr is invalid. $error")
            } else if (epcStr == null) {
                //throw ArgumentNullException("epcStr")
                throw Exception("ArgumentNullException epcStr")
            }

            this._epcStr = epcStr

            // if this is a GS1 class level epc (GS1 GTIN + Lot Number)
            if (epcStr.startsWith("urn:epc:id:sscc:")) {
                this.Type = EPCType.SSCC
                this.SerialLotNumber = epcStr.split(":").lastOrNull()
            } else if (epcStr.startsWith("urn:epc:class:lgtin:")) {
                this.Type = EPCType.Class

                val parts = epcStr.split(":").toMutableList()
                val parts2 = parts.last().split('.').toMutableList()
                parts.removeAt(parts.size - 1)

                val gtinStr = "${parts.joinToString(":")}:${parts2[0]}.${parts2[1]}"
                parts2[2]?.let { lotNumber ->
                    this.SerialLotNumber = lotNumber
                }
                gtinStr.replace(":class:lgtin:", ":idpat:sgtin:")
                this.GTIN = GTIN(gtinStr)
            }
            // else if this is a GS1 instance level epc (GS1 GTIN + Serial Number)
            else if (epcStr.startsWith("urn:epc:id:sgtin:")) {
                this.Type = EPCType.Instance

                val parts = epcStr.split(":").toMutableList()
                val parts2 = parts.last().split('.').toMutableList()
                parts.removeAt(parts.size - 1)

                val gtinStr = "${parts.joinToString(":")}:${parts2[0]}.${parts2[1]}"
                parts2[2]?.let { lotNumber ->
                    this.SerialLotNumber = lotNumber
                }
                gtinStr.replace(":id:sgtin:", ":idpat:sgtin:")
                this.GTIN = GTIN(gtinStr)
            }
            // else if this is a GDST / IBM private class level identifier (GTIN + Lot Number)
            else if (epcStr.startsWith("urn:") && epcStr.contains(":product:lot:class:")) {
                this.Type = EPCType.Class

                val parts = epcStr.split(":").toMutableList()
                val parts2 = parts.last().split('.').toMutableList()
                parts.removeAt(parts.size - 1)

                val gtinStr = "${parts.joinToString(":")}:${parts2[0]}.${parts2[1]}"
                parts2[2]?.let { lotNumber ->
                    this.SerialLotNumber = lotNumber
                }
                gtinStr.replace(":product:lot:class:", ":product:class:")
                this.GTIN = GTIN(gtinStr)
            }
            // else if this is a GDST / IBM private instance level identifier (GTIN + Serial Number)
            else if (epcStr.startsWith("urn:") && epcStr.contains(":product:serial:obj:")) {
                this.Type = EPCType.Instance

                val parts = epcStr.split(":").toMutableList()
                val parts2 = parts.last().split('.').toMutableList()
                parts.removeAt(parts.size - 1)

                val gtinStr = "${parts.joinToString(":")}:${parts2[0]}.${parts2[1]}"
                parts2[2]?.let { lotNumber ->
                    this.SerialLotNumber = lotNumber
                }
                gtinStr.replace(":product:serial:obj:", ":product:class:")
                this.GTIN = GTIN(gtinStr)
            } else if (epcStr.startsWith("urn:sscc:")) {
                this.Type = EPCType.SSCC
            } else if (epcStr.startsWith("urn:") && epcStr.contains(":lpn:obj:")) {
                this.Type = EPCType.SSCC
            } else if (epcStr.startsWith("urn:epc:id:bic:")) {
                this.Type = EPCType.SSCC
            } else if (isWellFormedUriString(epcStr) && epcStr.startsWith("http") && epcStr.contains("/obj/")) {
                this.Type = EPCType.Instance
                this.SerialLotNumber = epcStr.split('/').lastOrNull()
            } else if (isWellFormedUriString(epcStr) && epcStr.startsWith("http") && epcStr.contains("/class/")) {
                this.Type = EPCType.Class
                this.SerialLotNumber = epcStr.split('/').lastOrNull()
            } else if (isWellFormedUriString(epcStr)) {
                this.Type = EPCType.URI
            }
        } catch (ex: Exception) {
            val exception = Exception("The EPC is not in a valid format and could not be parsed. EPC=$epcStr", ex)
            opentraceability.OTLogger.error(ex)
            throw exception
        }

    }

    constructor(type: EPCType, gtin: GTIN, lotOrSerial: String) {
        if (type == EPCType.Class) {
            var epc = "${gtin.toString().lowercase()}.$lotOrSerial"
            if (epc.contains(":product:class:")) {
                epc = epc.replace(":product:class:", ":product:lot:class:")
            } else if (epc.contains(":idpat:sgtin:")) {
                epc = epc.replace(":idpat:sgtin:", ":class:lgtin:")
            } else {
                throw Exception("Unrecognized GTIN pattern. ${gtin.toString()}")
            }
            this.Type = type
            this.GTIN = gtin
            this.SerialLotNumber = lotOrSerial
            this._epcStr = epc
        } else if (type == EPCType.Instance) {
            var epc = "${gtin.toString().lowercase()}.$lotOrSerial"
            if (epc.contains(":product:class:")) {
                epc = epc.replace(":product:class:", ":product:serial:obj:")
            } else if (epc.contains(":idpat:sgtin:")) {
                epc = epc.replace(":idpat:sgtin:", ":id:sgtin:")
            } else {
                throw Exception("Unrecognized GTIN pattern. ${gtin.toString()}")
            }
            this.Type = type
            this.GTIN = gtin
            this.SerialLotNumber = lotOrSerial
            this._epcStr = epc
        } else {
            throw Exception("Cannot build EPC of type $type with a GTIN and Lot/Serial number.")
        }

    }

    companion object {
        fun DetectEPCIssue(epcStr: String?): String? {
            try {
                if (epcStr.isNullOrBlank()) {
                    return "The EPC is a NULL or White Space string."
                }

                // if this is a GS1 class level epc (GS1 GTIN + Lot Number)
                if (epcStr.startsWith("urn:epc:class:lgtin:")) {
                    if (!epcStr.IsURICompatibleChars()) {
                        return "The EPC contains non-compatible characters for a URN format."
                    }

                    val parts = epcStr.split(":")
                    val parts2 = parts.last().split('.')

                    if (parts2.size < 3) {
                        return "The EPC $epcStr is not in the right format. It doesn't contain a company prefix, item code, and lot number."
                    } else {
                        return null
                    }
                }
                // else if this is a GS1 instance level epc (GS1 GTIN + Serial Number)
                else if (epcStr.startsWith("urn:epc:id:sgtin:")) {
                    if (!epcStr.IsURICompatibleChars()) {
                        return "The EPC contains non-compatible characters for a URN format."
                    }

                    val parts = epcStr.split(":")
                    val parts2 = parts.last().split('.')

                    if (parts2.size < 3) {
                        return "The EPC $epcStr is not in the right format. It doesn't contain a company prefix, item code, and lot number."
                    } else {
                        return null
                    }
                }
                // else if this is a GDST / IBM private class level identifier (GTIN + Lot Number)
                else if (epcStr.startsWith("urn:") && epcStr.contains(":product:lot:class:")) {
                    if (!epcStr.IsURICompatibleChars()) {
                        return "The EPC contains non-compatible characters for a URN format."
                    }

                    val parts = epcStr.split(":")
                    val parts2 = parts.last().split('.')

                    if (parts2.size < 3) {
                        return "The EPC $epcStr is not in the right format. It doesn't contain a company prefix, item code, and serial number."
                    } else {
                        return null
                    }
                }
                // else if this is a GDST / IBM private instance level identifier (GTIN + Serial Number)
                else if (epcStr.startsWith("urn:") && epcStr.contains(":product:serial:obj:")) {
                    if (!epcStr.IsURICompatibleChars()) {
                        return "The EPC contains non-compatible characters for a URN format."
                    }

                    val parts = epcStr.split(":")
                    val parts2 = parts.last().split('.')

                    if (parts2.size < 3) {
                        return "The EPC $epcStr is not in the right format. It doesn't contain a company prefix, item code, and a serial number."
                    } else {
                        return null
                    }
                } else if (epcStr.startsWith("urn:epc:id:sscc:")) {
                    if (!epcStr.IsURICompatibleChars()) {
                        return "The EPC contains non-compatible characters for a URN format."
                    }

                    return null
                } else if (epcStr.startsWith("urn:epc:id:bic:")) {
                    if (!epcStr.IsURICompatibleChars()) {
                        return "The EPC contains non-compatible characters for a URN format."
                    }

                    return null
                } else if (epcStr.startsWith("urn:") && epcStr.contains(":lpn:obj:")) {
                    if (!epcStr.IsURICompatibleChars()) {
                        return "The EPC contains non-compatible characters for a URN format."
                    }

                    return null
                } else if (isWellFormedUriString(epcStr) && epcStr.startsWith("http") && epcStr.contains("/obj/")) {
                    return null
                } else if (isWellFormedUriString(epcStr) && epcStr.startsWith("http") && epcStr.contains("/class/")) {
                    return null
                } else if (isWellFormedUriString(epcStr)) {
                    return null
                } else {
                    return "This EPC does not fit any of the allowed formats."
                }
            } catch (ex: Exception) {
                opentraceability.OTLogger.error(ex)
                throw ex
            }
        }

        fun isWellFormedUriString(uriString: String): Boolean {
            try {
                URI(uriString).toURL()
                return true
            } catch (ex: Exception) {
                return false
            }
        }


        fun tryParse(epcStr: String?, epc: EPC?, error: String?): Boolean {

            var error: String? = error
            var epc: EPC? = epc

            try {
                error = EPC.DetectEPCIssue(epcStr)
                if (error.isNullOrBlank()) {
                    epc = EPC(epcStr)
                    return true
                } else {
                    epc = null
                    return false
                }
            } catch (ex: Exception) {
                opentraceability.OTLogger.error(ex)
                throw ex
            }
        }


    }

    fun matches(targetEPC: EPC): Boolean {
        if (this.equals(targetEPC)) {
            return true
        } else if (this.SerialLotNumber == "*" && this.GTIN == targetEPC.GTIN) {
            return true
        }
        return false
    }

    fun clone(): Any {
        val epc = EPC(this.toString())
        return epc
    }





    fun equals(obj1: EPC?, obj2: EPC?): Boolean {
        try {
            if (obj1 === null && obj2 === null) {
                return true
            }

            if (obj1 === null || obj2 === null) {
                return false
            }

            return obj1.equals(obj2)
        } catch (ex: Exception) {
            opentraceability.OTLogger.error(ex)
            throw ex
        }
    }

    fun notEquals(obj1: EPC?, obj2: EPC?): Boolean {
        try {
            if (obj1 === null && obj2 === null) {
                return false
            }

            if (obj1 === null || obj2 === null) {
                return true
            }

            return !obj1.equals(obj2)
        } catch (ex: Exception) {
            opentraceability.OTLogger.error(ex)
            throw ex
        }
    }

    override fun hashCode(): Int {
        try {
            return toString().hashCode()
        } catch (ex: Exception) {
            opentraceability.OTLogger.error(ex)
            throw ex
        }
    }

    override fun toString(): String {
        try {
            return _epcStr.lowercase()
        } catch (ex: Exception) {
            opentraceability.OTLogger.error(ex)
            throw ex
        }
    }

    override fun equals(other: Any?): Boolean {
        try {
            if (other === null) {
                return false
            }

            if (other === this) {
                return true
            }

            if (other.javaClass != this.javaClass) {
                return false
            }

            return isEquals(other as EPC)
        } catch (ex: Exception) {
            opentraceability.OTLogger.error(ex)
            throw ex
        }
    }

    fun isEquals(epc: EPC?): Boolean {
        try {
            if (epc === null) {
                return false
            }

            return this.toString().lowercase() == epc.toString().lowercase()
        } catch (ex: Exception) {
            opentraceability.OTLogger.error(ex)
            throw ex
        }
    }

    fun compareTo(epc: EPC?): Int {
        try {
            if (epc == null) {
                throw NullPointerException("epc")
            }

            val myInt64Hash = this.toString().getInt64HashCode()
            val otherInt64Hash = epc.toString().getInt64HashCode()

            if (myInt64Hash > otherInt64Hash) return -1
            if (myInt64Hash == otherInt64Hash) return 0

            return 1
        } catch (ex: Exception) {
            opentraceability.OTLogger.error(ex)
            throw ex
        }
    }


}
