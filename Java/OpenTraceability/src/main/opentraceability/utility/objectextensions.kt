package utility

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

object ObjectExtensions {
    fun String.getInt32HashCode(): Int {
        val hashCode: Int
        if (isNullOrEmpty()) {
            hashCode = 0
        } else {
            val bytes = this.toByteArray(StandardCharsets.UTF_8)
            val digest = MessageDigest.getInstance("CRC32")
            hashCode = digest.digest(bytes).toInt()
        }
        return hashCode
    }

    fun String.getInt16HashCode(): Short {
        val i64Hash = this.getInt64HashCode()
        var hash: Short = (i64Hash and 0xFFFF).toShort()
        hash = hash.xor((i64Hash shr 16 and 0xFFFF).toShort())
        hash = hash.xor((i64Hash shr 32 and 0xFFFF).toShort())
        hash = hash.xor((i64Hash shr 48 and 0xFFFF).toShort())
        return hash
    }

    fun String.getInt64HashCode(): Long {
        val hashCode: Long
        if (isNullOrEmpty()) {
            hashCode = 0L
        } else {
            val bytes = this.toByteArray(StandardCharsets.UTF_16LE)
            val digest = MessageDigest.getInstance("SHA-256")
            val hashText = digest.digest(bytes)
            val hashCodeStart = bytesToLong(hashText, 0)
            val hashCodeMedium = bytesToLong(hashText, 8)
            val hashCodeEnd = bytesToLong(hashText, 24)
            hashCode = hashCodeStart xor hashCodeMedium xor hashCodeEnd
        }
        return hashCode
    }

    private fun bytesToLong(bytes: ByteArray, offset: Int): Long {
        var result: Long = 0
        for (i in 0 until 8) {
            result = result shl 8
            result = result or (bytes[offset + i].toLong() and 0xFF)
        }
        return result
    }
}
