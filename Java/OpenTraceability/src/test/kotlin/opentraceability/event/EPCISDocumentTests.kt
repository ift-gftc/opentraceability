package opentraceability.event

import opentraceability.OpenTraceabilityTests
import opentraceability.mappers.OpenTraceabilityMappers
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.*

class EPCISDocumentTests {

    @ParameterizedTest
    @CsvFileSource(resources = ["opentraceability/data/xml_files.csv"], numLinesToSkip = 1)
    fun XML(file: String) {
        val xmlObjectEvents = OpenTraceabilityTests.readTestData(file)
        val doc = OpenTraceabilityMappers.EPCISDocument.XML.map(xmlObjectEvents)
        val xmlObjectEventsAfter = OpenTraceabilityMappers.EPCISDocument.XML.map(doc)
        OpenTraceabilityTests.compareXML(xmlObjectEvents, xmlObjectEventsAfter)
    }

    @ParameterizedTest
    @CsvFileSource(resources = ["jsonld_files.csv"], numLinesToSkip = 1)
    fun JSONLD(file: String) {
        val strEvents = OpenTraceabilityTests.readTestData(file)
        val doc = OpenTraceabilityMappers.EPCISDocument.JSON.map(strEvents)
        val strEventsAfter = OpenTraceabilityMappers.EPCISDocument.JSON.map(doc)
        OpenTraceabilityTests.compareJSON(strEvents, strEventsAfter)
    }
}