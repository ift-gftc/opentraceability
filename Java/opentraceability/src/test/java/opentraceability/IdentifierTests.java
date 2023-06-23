package opentraceability;

import opentraceability.models.identifiers.EPC;
import opentraceability.models.identifiers.EPCType;
import opentraceability.utility.EmbeddedResourceLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;

public class IdentifierTests {

    @Test
    public void EPCTests() throws Exception {
        String file = "epc_tests.json";

        Setup.Initialize();

        // load an XML file
        EmbeddedResourceLoader loader = new EmbeddedResourceLoader();
        String jsonStr = loader.readString(Setup.class, "/tests/" + file);

        JSONArray jarr = new JSONArray(jsonStr);

        for (Object o: jarr)
        {
            if (o instanceof JSONObject)
            {
                JSONObject jTestcase = (JSONObject)o;

                String epcStr = jTestcase.optString("epc");
                EPCType expectedType = Enum.valueOf(EPCType.class, jTestcase.optString("type"));
                String expectedGTIN = jTestcase.optString("gtin");
                String expectedLotOrSerial = jTestcase.optString("lotOrSerial");

                EPC epc = new EPC(epcStr);

                if (!epc.getType().equals(expectedType))
                {
                    throw new Exception("Failed to parse the EPC " + epcStr + ". The expected type was " + expectedType + " but we got " + epc.getType());
                }

                if (expectedGTIN != null)
                {
                    if (!epc.getGTIN().toString().equals(expectedGTIN))
                    {
                        throw new Exception("Failed to parse the EPC " + epcStr.toString() + ". The expected GTIN was " + expectedGTIN + " but we got " + epc.getGTIN().toString());
                    }
                }

                if (expectedLotOrSerial != null)
                {
                    if (!epc.getSerialLotNumber().equals(expectedLotOrSerial))
                    {
                        throw new Exception("Failed to parse the EPC " + epcStr.toString() + ". The expected Lot or Serial was " + expectedLotOrSerial + " but we got " + epc.getSerialLotNumber());
                    }
                }
            }
        }
    }
}
