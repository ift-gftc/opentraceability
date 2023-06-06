package opentraceability.event

import opentraceability.OpenTraceabilityTests
import opentraceability.mappers.OpenTraceabilityMappers
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.*

class EPCISDocumentTests {

    @Test
    //@ParameterizedTest
    //@CsvFileSource(resources = ["xml_files.csv"], numLinesToSkip = 1)
    fun XML() {

        var csvFileSource: String = "src/test/kotlin/opentraceability/data/xml_files.csv"
        val str = OpenTraceabilityTests.loadFile(csvFileSource)

        val substrings = str.split("\r\n")

        for (file in substrings) {

            try {
                val xmlObjectEvents = OpenTraceabilityTests.readTestData(file)
                val doc = OpenTraceabilityMappers.EPCISDocument.XML.map(xmlObjectEvents)
                val xmlObjectEventsAfter = OpenTraceabilityMappers.EPCISDocument.XML.map(doc)
                OpenTraceabilityTests.compareXML(xmlObjectEvents, xmlObjectEventsAfter)
            } catch (ex: Exception) {
                throw ex
            }


        }

    }

    @Test
    //@ParameterizedTest
    //@CsvFileSource(resources = ["jsonld_files.csv"], numLinesToSkip = 1)
    fun JSONLD() {

        var csvFileSource: String = "src/test/kotlin/opentraceability/data/jsonld_files.csv"
        val str = OpenTraceabilityTests.loadFile(csvFileSource)

        val substrings = str.split("\r\n")

        for (file in substrings) {

            try {
                val strEvents = OpenTraceabilityTests.readTestData(file)
                val doc = OpenTraceabilityMappers.EPCISDocument.JSON.map(strEvents)
                val strEventsAfter = OpenTraceabilityMappers.EPCISDocument.JSON.map(doc)
                OpenTraceabilityTests.compareJSON(strEvents, strEventsAfter)
            } catch (ex: Exception) {
                throw ex
            }
        }
    }
}