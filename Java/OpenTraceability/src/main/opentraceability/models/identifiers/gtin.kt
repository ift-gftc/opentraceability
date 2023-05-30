package models.identifiers

import utility.ObjectExtensions.getInt64HashCode
import utility.StringExtensions.isOnlyDigits
import utility.StringExtensions.isURICompatibleChars
import java.lang.Exception
import java.lang.NullPointerException
import java.lang.StringBuilder

class GTIN : Comparable<GTIN> {

    var _gtinStr: String = ""

    constructor() {
    }

    constructor(gtinStr: String?) {
        try {
            val error = GTIN.detectGTINIssue(gtinStr)
            if (!error.isNullOrEmpty()) {
                throw Exception("The GTIN $gtinStr is invalid. $error")
            }
            this._gtinStr = gtinStr!!
        } catch (ex: Exception) {
            OTLogger.error(ex)
            throw ex
        }

    }

    companion object {
        fun tryParse(gtinStr: String?): Pair<GTIN?, String?> {
            try {
                val error = detectGTINIssue(gtinStr)
                return if (error.isNullOrBlank()) Pair(GTIN(gtinStr), null) else Pair(null, error)
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
                    val lastPiece = gtinStr.split(':').last().replace(".", "")
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
                val exception = Exception("Failed to detect GTIN Issues. GTIN=$gtinStr", ex)
                OTLogger.error(exception)
                throw exception
            }
        }
    }

    fun isGS1GTIN(): Boolean {
        return _gtinStr?.contains(":idpat:sgtin:") ?: false
    }

    fun toDigitalLinkURL(): String {
        try {
            if (_gtinStr == null) {
                return ""
            } else if (isGS1GTIN()) {
                val gtinParts = _gtinStr.split(':').last().split('.')
                val gtin14 = gtinParts[1][0] + gtinParts[0] + gtinParts[1].drop(1)
                val gtinWithChecksum = gtin14 + GS1Util.calculateGTIN14CheckSum(gtin14)
                return "01/$gtinWithChecksum"
            } else {
                return "01/$_gtinStr"
            }
        } catch (ex: Exception) {
            OTLogger.error(ex)
            throw ex
        }
    }

    override fun equals(other: Any?): Boolean {
        try {
            if (this === other) {
                return true
            }

            if (other !is GTIN) {
                return false
            }

            return toString().toLowerCase() == other.toString().toLowerCase()
        } catch (ex: Exception) {
            OTLogger.error(ex)
            throw ex
        }
    }

    override fun hashCode(): Int {
        try {
            return toString().toLowerCase().hashCode()
        } catch (ex: Exception) {
            OTLogger.error(ex)
            throw ex
        }
    }

    override fun toString(): String {
        try {
            return _gtinStr?.toLowerCase() ?: ""
        } catch (ex: Exception) {
            OTLogger.error(ex)
            throw ex
        }
    }

    override fun compareTo(other: GTIN): Int {
        try {
            val myInt64Hash = toString().getInt64HashCode()
            val otherInt64Hash = other.toString().getInt64HashCode()
            return myInt64Hash.compareTo(otherInt64Hash)
        } catch (ex: Exception) {
            OTLogger.error(ex)
            throw ex
        }
    }
}
