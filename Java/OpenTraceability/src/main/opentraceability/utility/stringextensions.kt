package utility

import java.time.OffsetDateTime

class StringExtensions {
    companion object {

        var _digitsOnlyRegex: Regex = Regex("^[0-9]+\$")
        var _isURICompatibleCharsRegex: Regex = Regex("(.*[^._\\-:0-9A-Za-z])")

        fun IsOnlyDigits(str: String): Boolean {
            TODO("Not yet implemented")
        }

        fun TryConvertToDateTimeOffset(str: String): OffsetDateTime? {
            TODO("Not yet implemented")
        }

        fun IsURICompatibleChars(str: String): Boolean {
            TODO("Not yet implemented")
        }

        fun SplitXPath(str: String): List<String> {
            TODO("Not yet implemented")
        }
    }
}
