package models.identifiers


//[DataContract]
//[JsonConverter(typeof(GTINConverter))]
class GTIN /*: IEquatable<GTIN>, IComparable<GTIN>*/{

    internal var _gtinStr: String = ""

    constructor() {
    }

    constructor(gtinStr: String?) {
        try {
            val error = GTIN.detectGTINIssue(gtinStr)
            if (!error.isNullOrEmpty()) {
                throw Exception("The GTIN $gtinStr is invalid. $error")
            }
            this._gtinStr = gtinStr
        } catch (ex: Exception) {
            OTLogger.error(ex)
            throw ex
        }

    }

    fun isGS1GTIN(): Boolean {
        return _gtinStr.contains(":idpat:sgtin:")
    }


    fun toDigitalLinkURL(): String {
        try {
            if (_gtinStr.isNullOrEmpty()) {
                return ""
            } else if (isGS1GTIN()) {
                val gtinParts = _gtinStr.split(":").last().split(".")
                val gtin14 = "${gtinParts[1][0]}${gtinParts[0]}${gtinParts[1].substring(1)}" +
                        GS1Util.calculateGTIN14CheckSum("${gtinParts[1][0]}${gtinParts[0]}${gtinParts[1].substring(1)}")
                return "01/$gtin14"
            } else {
                return "01/$_gtinStr"
            }
        } catch (ex: Exception) {
            OTLogger.error(ex)
            throw ex
        }
    }


    companion object {
        fun detectGTINIssue(gtinStr: String?): String? {
            try {
                if (gtinStr.isNullOrEmpty()) {
                    return "GTIN is NULL or EMPTY."
                } else if (!gtinStr.isURICompatibleChars()) {
                    return "The GTIN contains non-compatible characters for a URI."
                } else if (gtinStr.contains(" ")) {
                    return "GTIN cannot contain spaces."
                } else if (gtinStr.length == 14 && gtinStr.isOnlyDigits()) {
                    val checksum = GS1Util.calculateGTIN14CheckSum(gtinStr)
                    if (checksum != gtinStr.last()) {
                        return "The check sum did not calculate correctly. The expected check sum was $checksum. " +
                                "Please make sure to validate that you typed the GTIN correctly. It's possible the check sum " +
                                "was typed correctly but another number was entered wrong."
                    }
                    return null
                } else if (gtinStr.startsWith("urn:") && gtinStr.contains(":product:class:")) {
                    return null
                } else if (gtinStr.startsWith("urn:") && gtinStr.contains(":idpat:sgtin:")) {
                    val lastPiece = gtinStr.split(":").last().replace(".", "")
                    if (!lastPiece.isOnlyDigits()) {
                        return "This is supposed to be a GS1 GTIN based on the System Prefix and " +
                                "Data Type Prefix. That means the Company Prefix and Serial Numbers " +
                                "should only be digits. Found non-digit characters in the Company Prefix " +
                                "or Serial Number."
                    } else if (lastPiece.length != 13) {
                        return "This is supposed to be a GS1 GTIN based on the System Prefix and Data Type " +
                                "Prefix. That means the Company Prefix and Serial Numbers should contain a maximum " +
                                "total of 13 digits between the two. The total number of digits when combined " +
                                "is ${lastPiece.length}."
                    }
                    return null
                } else {
                    return "The GTIN is not in a valid EPCIS URI format or in GS1 GTIN-14 format."
                }
            } catch (ex: Exception) {
                OTLogger.error(ex)
                throw ex
            }
        }


        fun tryParse(gtinStr: String?, out gtin: GTIN?, out error: String?): Boolean {
            try {
                error = GTIN.detectGTINIssue(gtinStr)
                if (error.isNullOrEmpty()) {
                    gtin = GTIN(gtinStr)
                    return true
                } else {
                    gtin = null
                    return false
                }
            } catch (ex: Exception) {
                OTLogger.error(ex)
                throw ex
            }
        }

        fun isGTIN(gtinStr: String): Boolean {
            try {
                return detectGTINIssue(gtinStr) == null
            } catch (ex: Exception) {
                OTLogger.error(ex)
                throw ex
            }
        }

    }





    fun clone(): Any {
        return GTIN(toString())
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GTIN) return false
        return this.isEquals(other)
    }

    override fun hashCode(): Int {
        return toString().hashCode()
    }

    override fun toString(): String {
        return _gtinStr?.toLowerCase() ?: ""
    }

    fun equals(obj1: GTIN?, obj2: GTIN?): Boolean {
        if (obj1 === obj2) return true
        if (obj1 == null || obj2 == null) return false
        return obj1.equals(obj2)
    }

    fun notEquals(obj1: GTIN?, obj2: GTIN?): Boolean {
        return !equals(obj1, obj2)
    }




    fun equals(gtin: GTIN?): Boolean {
        if (gtin == null) return false
        if (this === gtin) return true
        return isEquals(gtin)
    }

    private fun isEquals(gtin: GTIN?): Boolean {
        if (gtin == null) return false
        return toString().equals(gtin.toString(), ignoreCase = true)
    }



    fun compareTo(gtin: GTIN?): Int {
        if (gtin == null) {
            throw IllegalArgumentException("gtin")
        }
        val myInt64Hash = toString().getInt64HashCode()
        val otherInt64Hash = gtin.toString().getInt64HashCode()
        return when {
            myInt64Hash > otherInt64Hash -> -1
            myInt64Hash == otherInt64Hash -> 0
            else -> 1
        }
    }

}
