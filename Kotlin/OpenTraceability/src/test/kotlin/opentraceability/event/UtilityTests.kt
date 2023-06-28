package opentraceability.event

import opentraceability.OpenTraceabilityTests
import org.junit.jupiter.api.Test

class UtilityTests {
    @Test
    //@ParameterizedTest(name = "{0}")
    //@ValueSource(strings = ["EPCISQueryDocument.jsonld"])
    fun jsonCompareTest() {
        val json1 = OpenTraceabilityTests.readTestData("EPCISQueryDocument.jsonld")
        val json2 = OpenTraceabilityTests.readTestData("EPCISQueryDocument.jsonld")
        OpenTraceabilityTests.compareJSON(json1, json2)
    }
}
