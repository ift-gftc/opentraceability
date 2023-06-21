package opentraceability.models.identifiers

import opentraceability.utility.*

class PGLN {

    internal var _pglnStr: String = ""

    constructor(){}

    constructor(pglnStr: String?) {
        val error = DetectPGLNIssue(pglnStr)
        if (!error.isNullOrBlank()) {
            throw Exception("The PGLN $pglnStr is not valid. $error")
        }
        if (pglnStr != null) {
            this._pglnStr = pglnStr
        }
        else{
            this._pglnStr = ""
        }
    }

    fun IsGS1PGLN(): Boolean {
        return _pglnStr.contains(":id:pgln:") || _pglnStr.contains(":id:sgln:")
    }


    fun ToDigitalLinkURL(): String {
        try {
            if (IsGS1PGLN()) {
                val gtinParts = _pglnStr.split(':').last().split('.')
                val pgln = gtinParts[0] + gtinParts[1]
                val calculatedCheckSum = GS1Util.CalculateGLN13CheckSum(pgln)
                val digitalLinkURL = "417/$pgln$calculatedCheckSum"
                return digitalLinkURL
            } else {
                val digitalLinkURL = "417/$_pglnStr"
                return digitalLinkURL
            }
        } catch (ex: Exception) {
            opentraceability.OTLogger.error(ex)
            throw ex
        }
    }


    companion object {
        fun IsPGLN(pglnStr: String): Boolean {
            try {
                if (PGLN.DetectPGLNIssue(pglnStr) == null) {
                    return true
                } else {
                    return false
                }
            } catch (ex: Exception) {
                opentraceability.OTLogger.error(ex)
                throw ex
            }
        }

        fun DetectPGLNIssue(pglnStr: String?): String? {
            try {
                if (pglnStr.isNullOrEmpty()) {
                    return "PGLN is NULL or EMPTY."
                } else if (pglnStr.contains(" ")) {
                    return "PGLN cannot contain spaces."
                } else if (!IsURICompatibleChars(pglnStr)) {
                    return "The PGLN contains non-compatible characters for a URI."
                } else if (pglnStr.length == 13 && IsOnlyDigits(pglnStr)) {
                    val checksum = GS1Util.CalculateGLN13CheckSum(pglnStr)
                    if (checksum != pglnStr.last()) {
                        return "The check sum did not calculate correctly. The expected check sum was $checksum. " +
                                "Please make sure to validate that you typed the PGLN correctly. It's possible the check sum " +
                                "was typed correctly but another number was entered wrong."
                    }
                    return null
                } else if (pglnStr?.startsWith("urn:") == true && pglnStr.contains(":party:")) {
                    return null
                } else if (pglnStr?.startsWith("urn:") == true && (pglnStr.contains(":id:pgln:") || pglnStr.contains(":id:sgln:"))) {
                    val pieces = pglnStr.split(':').last().split('.')
                    if (pieces.size < 2) {
                        return "This is supposed to contain the company prefix and the location code. Did not find these two pieces."
                    }
                    val lastPiece = pieces[0] + pieces[1]
                    if (!IsOnlyDigits(lastPiece)) {
                        return "This is supposed to be a GS1 PGLN based on the System Prefix and " +
                                "Data Type Prefix. That means the Company Prefix and Serial Numbers " +
                                "should only be digits. Found non-digit characters in the Company Prefix " +
                                "or Serial Number."
                    } else if (lastPiece.length != 12) {
                        return "This is supposed to be a GS1 PGLN based on the System Prefix and Data Type " +
                                "Prefix. That means the Company Prefix and Serial Numbers should contain a maximum " +
                                "total of 12 digits between the two. The total number of digits when combined " +
                                "is ${lastPiece.length}."
                    }
                    return null
                } else {
                    return "The PGLN is not in a valid EPCIS URI format or in GS1 (P)GLN-13 format. PGLN = $pglnStr"
                }
            } catch (ex: Exception) {
                opentraceability.OTLogger.error(ex)
                throw ex
            }
        }

        fun IsURICompatibleChars(input: String): Boolean {
            val reservedChars = ":/?#[]@!$&'()*+,;="
            val unreservedChars = "-._~"
            val allowedChars = reservedChars + unreservedChars + "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

            return input.all { allowedChars.contains(it) }
        }

        fun IsOnlyDigits(input: String): Boolean {
            val regex = Regex("\\d+")
            return regex.matches(input)
        }


        fun TryParsePGLN(pglnStr: String?, pgln: PGLN? = null): Pair<Boolean, PGLN?> {
            try {
                val error = DetectPGLNIssue(pglnStr)
                if (error == null) {
                    val parsedPGLN = PGLN(pglnStr)
                    return Pair(true, parsedPGLN)
                } else {
                    return Pair(false, null)
                }
            } catch (ex: Exception) {
                opentraceability.OTLogger.error(ex)
                throw ex
            }
        }

    }



    fun clone(): PGLN {
        val pgln = PGLN(this.toString())
        return pgln
    }



    fun notEquals(obj: Any?): Boolean {
        return !(this == obj)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PGLN) return false
        return this.isEquals(other)
    }

    override fun hashCode(): Int {
        return this.toString().toLowerCase().hashCode()
    }

    override fun toString(): String {
        return this._pglnStr?.toLowerCase() ?: ""
    }





    fun equals(pgln: PGLN?): Boolean {
        if (pgln == null) return false
        if (this === pgln) return true
        return isEquals(pgln)
    }

    fun isEquals(pgln: PGLN?): Boolean {
        if (pgln == null) return false
        return toString().equals(pgln.toString(), ignoreCase = true)
    }



    fun compareTo(pgln: PGLN?): Int {
        if (pgln == null) {
            throw IllegalArgumentException("pgln")
        }
        val myInt64Hash = toString().getInt64HashCode()
        val otherInt64Hash = pgln.toString().getInt64HashCode()
        return when {
            myInt64Hash > otherInt64Hash -> -1
            myInt64Hash == otherInt64Hash -> 0
            else -> 1
        }
    }

}
