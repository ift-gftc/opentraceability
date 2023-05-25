package utility

import java.time.OffsetDateTime

class StringExtensions {
    companion object {

        var _digitsOnlyRegex: Regex = Regex("^[0-9]+\$")
        var _isURICompatibleCharsRegex: Regex = Regex("(.*[^._\\-:0-9A-Za-z])")

        fun IsOnlyDigits(str: String): Boolean {
            try {
                return _digitsOnlyRegex.matches(str)
            } catch (ex: Exception) {
                OTLogger.Error(ex)
                throw ex
            }
        }

        fun TryConvertToDateTimeOffset(str: String): OffsetDateTime? {
            TODO("Not yet implemented")
        }

        fun IsURICompatibleChars(str: String): Boolean {
            try {
                return !_isURICompatibleCharsRegex.matches(str)
            } catch (ex: Exception) {
                OTLogger.Error(ex)
                throw ex
            }
        }

        fun SplitXPath(str: String): ArrayList<String> {

            var str2: String = str

            var r: Regex  = Regex("(?=[^{}]*(?:{[^{}]*}[^{}]*)*$)\\/");

            while (r.matches(str2))
            {
                str2 = r.replace(str2, "%SLASH%");
            }

            return ArrayList(str2.split("%SLASH%"))
        }
    }
}
