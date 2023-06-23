package opentraceability.models.identifiers

import opentraceability.utility.*
import opentraceability.OTLogger


//[DataContract]
//[JsonConverter(typeof(GLNConverter))]
class GLN /*: IEquatable<GLN>, IComparable<GLN>*/ {

    internal var _glnStr: String = ""


    constructor(){}

    constructor(glnStr: String) {
        try {
            val error = GLN.DetectGLNIssue(glnStr)
            if (!error.isNullOrBlank()) {
                throw Exception("The GLN $glnStr is invalid. $error")
            }
            this._glnStr = glnStr
        } catch (ex: Exception) {
            opentraceability.OTLogger.error(ex)
            throw ex
        }

    }

    fun ToDigitalLinkURL(): String {
        return try {
            if (IsGS1PGLN()) {
                val gtinParts = _glnStr.split(':').last().split('.')
                val pgln = gtinParts[0] + gtinParts[1] + GS1Util.CalculateGLN13CheckSum(gtinParts[0] + gtinParts[1])
                "414/$pgln"
            } else {
                "414/$_glnStr"
            }
        } catch (ex: Exception) {
            opentraceability.OTLogger.error(ex)
            throw ex
        }
    }



    companion object {
        fun isGLN(glnStr: String): Boolean {
            return try {
                DetectGLNIssue(glnStr) == null
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



        fun DetectGLNIssue(glnStr: String): String? {
            return try {
                if (glnStr.isNullOrEmpty()) {
                    return "The GLN is NULL or EMPTY."
                } else if (!IsURICompatibleChars(glnStr)) {
                    return "The GLN contains non-compatiable characters for a URI."
                } else if (glnStr.contains(" ")) {
                    return "GLN cannot contain spaces."
                } else if (glnStr.length == 13 && IsOnlyDigits(glnStr)) {
                    val checksum = GS1Util.CalculateGLN13CheckSum(glnStr)
                    if (checksum != glnStr.last()) {
                        return "The check sum did not calculate correctly. The expected check sum was $checksum. " +
                                "Please make sure to validate that you typed the GLN correctly. It's possible the check sum " +
                                "was typed correctly but another number was entered wrong."
                    }
                    null
                } else if (glnStr.startsWith("urn:") && glnStr.contains(":location:loc:")) {
                    null
                } else if (glnStr.startsWith("urn:") && glnStr.contains(":location:extension:loc:")) {
                    null
                } else if (glnStr.contains(":id:sgln:")) {
                    val pieces = glnStr.split(':').last().split('.')
                    if (pieces.size < 2) {
                        throw Exception("The GLN $glnStr is not valid.")
                    }
                    val lastPiece = pieces[0] + pieces[1]
                    if (!IsOnlyDigits(lastPiece)) {
                        return "This is supposed to be a GS1 GLN based on the System Prefix and " +
                                "Data Type Prefix. That means the Company Prefix and Serial Numbers " +
                                "should only be digits. Found non-digit characters in the Company Prefix " +
                                "or Serial Number."
                    } else if (lastPiece.length != 12) {
                        return "This is supposed to be a GS1 GLN based on the System Prefix and Data Type " +
                                "Prefix. That means the Company Prefix and Serial Numbers should contain a maximum " +
                                "total of 12 digits between the two. The total number of digits when combined " +
                                "is ${lastPiece.length}."
                    }
                    null
                } else {
                    "The GLN is not in a valid EPCIS URI format or in GS1 GLN-13 format. Value = $glnStr"
                }
            } catch (ex: Exception) {
                opentraceability.OTLogger.error(ex)
                throw ex
            }
        }

        fun TryParse(glnStr: String, gln: GLN?, error: String?): Boolean {

            var gln: GLN? = gln
            var error: String? = error

            return try {
                error = DetectGLNIssue(glnStr)
                if (error.isNullOrEmpty()) {
                    gln = GLN(glnStr)
                    true
                } else {
                    gln = null
                    false
                }
            } catch (ex: Exception) {
                opentraceability.OTLogger.error(ex)
                throw ex
            }
        }
    }

    fun IsGS1PGLN(): Boolean {
        return _glnStr.contains(":id:sgln:")
    }

    fun Clone(): Any {
        val gln = GLN(toString())
        return gln
    }



    fun equals(obj1: GLN?, obj2: GLN?): Boolean {
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

    fun notEquals(obj1: GLN?, obj2: GLN?): Boolean {
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

    override fun equals(obj: Any?): Boolean {
        try {
            if (obj == null) {
                return false
            }

            if (this === obj) {
                return true
            }

            if (javaClass != obj.javaClass) {
                return false
            }

            return isEquals(obj as GLN)
        } catch (ex: Exception) {
            opentraceability.OTLogger.error(ex)
            throw ex
        }
    }

    override fun hashCode(): Int {
        try {
            val hash = toString().hashCode()
            return hash
        } catch (ex: Exception) {
            opentraceability.OTLogger.error(ex)
            throw ex
        }
    }

    override fun toString(): String {
        try {
            return _glnStr.toLowerCase()
        } catch (ex: Exception) {
            opentraceability.OTLogger.error(ex)
            throw ex
        }
    }

    fun equals(gln: GLN?): Boolean {
        try {
            if (gln == null) {
                return false
            }

            if (this === gln) {
                return true
            }

            return toString() == gln.toString()
        } catch (ex: Exception) {
            opentraceability.OTLogger.error(ex)
            throw ex
        }
    }

     fun isEquals(gln: GLN): Boolean {
        try {
            if (gln == null) {
                return false
            }

            return toString().toLowerCase() == gln.toString().toLowerCase()
        } catch (ex: Exception) {
            opentraceability.OTLogger.error(ex)
            throw ex
        }
    }

    fun compareTo(gln: GLN?): Int {
        try {
            if (gln == null) {
                throw NullPointerException("gln")
            }

            val myInt64Hash = toString().getInt64HashCode()
            val otherInt64Hash = gln.toString().getInt64HashCode()

            return when {
                myInt64Hash > otherInt64Hash -> -1
                myInt64Hash == otherInt64Hash -> 0
                else -> 1
            }
        } catch (ex: Exception) {
            opentraceability.OTLogger.error(ex)
            throw ex
        }
    }


}
