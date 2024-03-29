package opentraceability.identifiers

import opentraceability.OpenTraceabilityTests
import opentraceability.models.identifiers.*
import org.junit.jupiter.params.ParameterizedTest
import org.json.JSONArray
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertFails

class IdentifierTests {

    @Test
    //@ParameterizedTest
    //@ValueSource(strings = ["epc_tests.json"])
    fun EPCTests() {
        val jsonStr = OpenTraceabilityTests.readTestData("epc_tests.json")
        val jarr = JSONArray(jsonStr)

        for (i in 0 until jarr.length()) {
            val jTestcase = jarr.getJSONObject(i)
            val epcStr = jTestcase.optString("epc")
            val expectedType = EPCType.valueOf(jTestcase.optString("type", ""))
            val expectedGTIN = jTestcase.optString("gtin", null)
            val expectedLotOrSerial = jTestcase.optString("lotOrSerial", null)

            var (valid, err) = EPC.tryParse(epcStr)

            if (!valid) {
                assert(false){"Failed EPC.tryParse $err"}
            }

            val epc = EPC(epcStr)

            if (epc.Type != expectedType) {
                assert(false){"Failed to parse the EPC $epcStr. The expected type was $expectedType but we got ${epc.Type}"}
            }

            if (expectedGTIN != null) {
                if (epc.GTIN?.toString() != expectedGTIN) {
                    assert(false) { "Failed to parse the EPC $epcStr. The expected GTIN was $expectedGTIN but we got ${epc.GTIN?.toString()}"}
                }
            }

            if (expectedLotOrSerial != null) {
                if (epc.SerialLotNumber != expectedLotOrSerial) {
                    assert(false){"Failed to parse the EPC $epcStr. The expected Lot or Serial was $expectedLotOrSerial but we got ${epc.SerialLotNumber}"}
                }
            }
        }
    }
}