package opentraceability.utility

import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.StringReader
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.xml.parsers.DocumentBuilderFactory

object StringExtensions {
    private val digitsOnlyRegex = Regex("^[0-9]+$")

    fun String.IsOnlyDigits(): Boolean {
        return digitsOnlyRegex.matches(this)
    }

    private val IsURICompatibleCharsRegex = Regex("(.*[^._\\-:0-9A-Za-z])")

    fun String.IsURICompatibleChars(): Boolean {
        return !IsURICompatibleCharsRegex.matches(this)
    }

    fun String.tryConvertToDateTimeOffset(): OffsetDateTime? {
        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        return try {
            OffsetDateTime.parse(this, formatter)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    fun String.toDuration(): Duration {
        val parts = split(":")
        val hours = parts[0].toLong()
        val minutes = parts[1].toLong()
        return Duration.ofHours(hours).plusMinutes(minutes)
    }

    fun String.splitXPath(): MutableList<String> {
        val regex = "(?=[^{}]*(?:{[^{}]*}[^{}]*)*\$)/"
        var str = this.replace(regex, "%SLASH%")
        return str.split("%SLASH%").toMutableList()
    }


    fun String.parseXmlToDocument(): Document {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val inputSource = InputSource(StringReader(this))
        return builder.parse(inputSource)
    }
}
