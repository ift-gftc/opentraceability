package opentraceability.utility

import opentraceability.utility.StringExtensions.removeBOM
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

        val cleanedXml = this.removeBOM()

        var xDoc:Document? = null

        try {
            val factory = DocumentBuilderFactory.newInstance()
            factory.isNamespaceAware = true
            val builder = factory.newDocumentBuilder()
            val inputSource = InputSource(StringReader(cleanedXml))
            xDoc = builder.parse(inputSource)
        }
        catch (ex: Exception){
            throw ex
        }

        return xDoc
    }

    fun String.removeBOM(): String {
        val bomMarkers = listOf(0xEF.toChar(), 0xBB.toChar(), 0xBF.toChar())
        return if (bomMarkers.all { this.firstOrNull() == it }) {
            this.drop(1)
        } else {
            this
        }
    }


}
