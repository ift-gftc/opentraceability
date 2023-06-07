package opentraceability.event

import opentraceability.OpenTraceabilityTests
import opentraceability.mappers.epcis.xml.EPCISDocumentBaseXMLMapper
import opentraceability.utility.StringExtensions.parseXmlToDocument
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class SchemaCheckTests {
    @Test
    //@ParameterizedTest(name = "{0}")
    //@CsvSource("querydoc_fail_schemacheck.xml,false")
    fun epcisQueryDocumentXML12() {
        val xmlObjectEvents = OpenTraceabilityTests.readTestData("querydoc_fail_schemacheck.xml")
        val xDoc = xmlObjectEvents.parseXmlToDocument()

        val exceptionThrown = try {
            EPCISDocumentBaseXMLMapper.validateEPCISQueryDocumentSchema(xDoc, opentraceability.models.events.EPCISVersion.V1)
            false
        } catch (e: Exception) {
            true
        }

        Assertions.assertEquals(false, exceptionThrown)
    }
}
