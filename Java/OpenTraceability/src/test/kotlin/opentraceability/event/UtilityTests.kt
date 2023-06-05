package opentraceability.event

import opentraceability.OpenTraceabilityTests
import org.junit.jupiter.api.Test

class UtilityTests {
    @Test
    fun jsonCompareTest(file: String) {
        val json1 = OpenTraceabilityTests.readTestData(file)
        val json2 = OpenTraceabilityTests.readTestData(file)
        OpenTraceabilityTests.compareJSON(json1, json2)
    }
}
