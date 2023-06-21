package opentraceability.models.identifiers

object GS1Util {
    fun IsEven(i: Int): Boolean {
        return i % 2 == 0
    }

    fun CharToInt32(charInt: Char): Int {
        return when (charInt) {
            '0' -> 0
            '1' -> 1
            '2' -> 2
            '3' -> 3
            '4' -> 4
            '5' -> 5
            '6' -> 6
            '7' -> 7
            '8' -> 8
            '9' -> 9
            else -> throw IllegalArgumentException("Must give a single digit numeral string.")
        }
    }

    fun Int32ToChar(charInt: Int): Char {
        return when (charInt) {
            0 -> '0'
            1 -> '1'
            2 -> '2'
            3 -> '3'
            4 -> '4'
            5 -> '5'
            6 -> '6'
            7 -> '7'
            8 -> '8'
            9 -> '9'
            else -> throw IllegalArgumentException("Must give a single digit numeral string.")
        }
    }

    fun BreakIntoDigits(strInt: String): IntArray {
        val rtnInts = mutableListOf<Int>()

        for (i in 0 until strInt.length) {
            rtnInts.add(CharToInt32(strInt[i]))
        }

        return rtnInts.toIntArray()
    }

    fun CalculateGTIN14CheckSum(strGS: String): Char {
        requireNotNull(strGS) { "strGS cannot be null." }

        val gsDigits = BreakIntoDigits(strGS)
        var sum = 0

        for (i in gsDigits.indices) {
            if (IsEven(i)) {
                sum += gsDigits[i] * 3
            } else {
                sum += gsDigits[i]
            }
        }

        var higherMultipleOfTen = 10
        while (higherMultipleOfTen < sum) {
            higherMultipleOfTen += 10
        }
        if (sum == 0) {
            higherMultipleOfTen = 0
        }

        val determinedCheckSum = higherMultipleOfTen - sum
        val charCheckSum = Int32ToChar(determinedCheckSum)
        return charCheckSum
    }

    fun CalculateGLN13CheckSum(strGS: String): Char {
        requireNotNull(strGS) { "strGS cannot be null." }

        val gsDigits = BreakIntoDigits(strGS)
        var sum = 0

        for (i in gsDigits.indices) {
            if (IsEven(i)) {
                sum += gsDigits[i]
            } else {
                sum += gsDigits[i] * 3
            }
        }

        var higherMultipleOfTen = 10
        while (higherMultipleOfTen < sum) {
            higherMultipleOfTen += 10
        }

        val determinedCheckSum = higherMultipleOfTen - sum
        val charCheckSum = Int32ToChar(determinedCheckSum)
        return charCheckSum
    }
}
