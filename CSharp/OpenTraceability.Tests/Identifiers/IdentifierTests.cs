using Newtonsoft.Json.Linq;
using OpenTraceability.Models.Identifiers;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Tests.Identifiers
{
    [TestFixture]
    public class IdentifierTests
    {
        [Test]
        [TestCase("epc_tests.json")]
        public void EPCTests(string file)
        {
            string jsonStr = OpenTraceabilityTests.ReadTestData(file);
            JArray jarr = JArray.Parse(jsonStr);
            foreach (JObject jTestcase in jarr)
            {
                string epcStr = jTestcase["epc"]?.ToString();
                EPCType expectedType = Enum.Parse<EPCType>(jTestcase.Value<string>("type"));
                string? expectedGTIN = jTestcase.Value<string>("gtin");
                string? expectedLotOrSerial = jTestcase.Value<string>("lotOrSerial");

                EPC epc = new EPC(epcStr);

                if (epc.Type != expectedType)
                {
                    Assert.Fail($"Failed to parse the EPC {epcStr}. The expected type was {expectedType} but we got {epc.Type}");
                }

                if (expectedGTIN != null)
                {
                    if (epc.GTIN?.ToString() != expectedGTIN)
                    {
                        Assert.Fail($"Failed to parse the EPC {epcStr}. The expected GTIN was {expectedGTIN} but we got {epc.GTIN?.ToString()}");
                    }
                }

                if (expectedLotOrSerial != null)
                {
                    if (epc.SerialLotNumber != expectedLotOrSerial)
                    {
                        Assert.Fail($"Failed to parse the EPC {epcStr}. The expected Lot or Serial was {expectedLotOrSerial} but we got {epc.SerialLotNumber}");
                    }
                }
            }
        }
    }
}
