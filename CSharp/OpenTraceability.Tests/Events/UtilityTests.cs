using System;
namespace OpenTraceability.Tests.Events
{
	[TestFixture]
	public class UtilityTests
	{
		[Test]
		[TestCase("EPCISQueryDocument.jsonld")]
		public void JSONCompareTest(string file)
		{
			string json1 = OpenTraceabilityTests.ReadTestData(file);
			string json2 = OpenTraceabilityTests.ReadTestData(file);
			OpenTraceabilityTests.CompareJSON(json1, json2);
        }
	}
}

