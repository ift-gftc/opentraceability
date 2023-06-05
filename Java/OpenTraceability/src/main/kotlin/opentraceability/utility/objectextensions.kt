package opentraceability.utility

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import kotlin.experimental.xor

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

fun bytesToLong(bytes: ByteArray, offset: Int): Long {
    var result: Long = 0
    for (i in 0 until 8) {
        result = result shl 8
        result = result or (bytes[offset + i].toLong() and 0xFF)
    }
    return result
}

fun ByteArray.toInt(): Int {
    if (size < 4) {
        throw IllegalArgumentException("ByteArray must have at least 4 elements")
    }
    return (this[0].toInt() and 0xFF) shl 24 or
            (this[1].toInt() and 0xFF) shl 16 or
            (this[2].toInt() and 0xFF) shl 8 or
            (this[3].toInt() and 0xFF)
}
