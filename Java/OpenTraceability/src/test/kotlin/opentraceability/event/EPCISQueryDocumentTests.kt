package opentraceability.event


import opentraceability.OpenTraceabilityTests
import opentraceability.mappers.OpenTraceabilityMappers
import opentraceability.models.events.EPCISVersion
import opentraceability.utility.element
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.w3c.dom.Element

class EPCISQueryDocumentTests {
    @Test
    fun XML() {
        var file: String = "querydoc_example01.xml"

        // read object events from test data specified in the file argument
        val xmlObjectEvents = OpenTraceabilityTests.readTestData(file)
        val doc = OpenTraceabilityMappers.EPCISQueryDocument.XML.map(xmlObjectEvents)
        var xmlObjectEventsAfter = OpenTraceabilityMappers.EPCISQueryDocument.XML.map(doc)
        OpenTraceabilityTests.compareXML(xmlObjectEvents, xmlObjectEventsAfter)
        doc.header =  opentraceability.models.common.StandardBusinessDocumentHeader.DummyHeader
        xmlObjectEventsAfter = OpenTraceabilityMappers.EPCISQueryDocument.XML.map(doc)
    }

    @Test
    fun readCrazyXML() {
        val file = "querydoc_example02.xml"
        val xmlObjectEvents = OpenTraceabilityTests.readTestData(file)
        val doc = OpenTraceabilityMappers.EPCISQueryDocument.XML.map(xmlObjectEvents, false)
    }

    @Test
    fun XML12ToJSONLD() {
        val file = "gdst_extensions_01.xml"
        val stringXmlEvents = OpenTraceabilityTests.readTestData(file)
        val doc = OpenTraceabilityMappers.EPCISQueryDocument.XML.map(stringXmlEvents)
        doc.header = null
        doc.epcisVersion = EPCISVersion.V2
        val jsonLD = OpenTraceabilityMappers.EPCISQueryDocument.JSON.map(doc)
        val docAfter = OpenTraceabilityMappers.EPCISQueryDocument.JSON.map(jsonLD)
        docAfter.header = null
        docAfter.epcisVersion = EPCISVersion.V1
        var xmlAfter = OpenTraceabilityMappers.EPCISQueryDocument.XML.map(docAfter)
        var finalDoc = OpenTraceabilityMappers.EPCISQueryDocument.XML.map(xmlAfter)
        xmlAfter = xmlAfter.replace("https://ref.gs1.org/cbv/Disp-", "urn:epcglobal:cbv:disp:")
        xmlAfter = xmlAfter.replace("https://ref.gs1.org/cbv/BizStep-", "urn:epcglobal:cbv:bizstep:")
        xmlAfter = xmlAfter.replace("https://ref.gs1.org/cbv/SDT-", "urn:epcglobal:cbv:sdt:")
        val x1 =  opentraceability.utility.createXmlElement(stringXmlEvents).element("EPCISBody")
            ?: throw Exception("failed to grab EPCISBody element from the XML for stringXmlEvents=$stringXmlEvents")
        val x2 = opentraceability.utility.createXmlElement(xmlAfter).element("EPCISBody")
            ?: throw Exception("failed to grab EPCISBody element from the XML for xmlAfter=$xmlAfter")
        OpenTraceabilityTests.compareXML(x1.toString(), x2.toString())
    }

    @Test
    fun JSONLD() {

        OpenTraceabilityTests()

        val file = "EPCISQueryDocument.jsonld"
        val json = OpenTraceabilityTests.readTestData(file)
        val doc = OpenTraceabilityMappers.EPCISQueryDocument.JSON.map(json, false)
        val jsonAfter = OpenTraceabilityMappers.EPCISQueryDocument.JSON.map(doc)
        OpenTraceabilityTests.compareJSON(json, jsonAfter)
    }

}