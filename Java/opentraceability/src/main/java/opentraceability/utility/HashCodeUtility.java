package opentraceability.utility;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.zip.CRC32;

public class HashCodeUtility {
    public static int getInt32HashCode(String str) {
        int hashCode;
        if (str == null || str.isEmpty()) {
            hashCode = 0;
        } else {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            CRC32 fileCRC32 = new CRC32();
            fileCRC32.update(bytes);
            hashCode = (int) fileCRC32.getValue();
        }
        return hashCode;
    }

    public static short getInt16HashCode(String str) {
        long i64Hash = getInt64HashCode(str);
        short hash = (short) (i64Hash & 0xFFFF);
        hash = (short) (hash ^ (i64Hash >> 16 & 0xFFFF));
        hash = (short) (hash ^ (i64Hash >> 32 & 0xFFFF));
        hash = (short) (hash ^ (i64Hash >> 48 & 0xFFFF));
        return hash;
    }

    public static long getInt64HashCode(String str) {
        long hashCode;
        if (str == null || str.isEmpty()) {
            hashCode = 0L;
        } else {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_16LE);
            MessageDigest digest;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            byte[] hashText = digest.digest(bytes);
            long hashCodeStart = bytesToLong(hashText, 0);
            long hashCodeMedium = bytesToLong(hashText, 8);
            long hashCodeEnd = bytesToLong(hashText, 24);
            hashCode = hashCodeStart ^ hashCodeMedium ^ hashCodeEnd;
        }
        return hashCode;
    }

    public static long bytesToLong(byte[] bytes, int offset) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result = result << 8;
            result = result | (bytes[offset + i] & 0xFF);
        }
        return result;
    }

    public static int toInt(byte[] bytes) {
        if (bytes.length < 4) {
            throw new IllegalArgumentException("ByteArray must have at least 4 elements");
        }
        return (bytes[0] & 0xFF) << 24 |
                (bytes[1] & 0xFF) << 16 |
                (bytes[2] & 0xFF) << 8 |
                (bytes[3] & 0xFF);
    }
}