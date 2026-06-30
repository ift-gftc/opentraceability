using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;
using System;
using System.Collections.Generic;
using System.Text;

namespace OpenTraceability.Tests.Events
{
    /// <summary>
    /// The goal of these tests is to demonstrate that we can transform data from GDST version 1.2 to GDST version 2.0 and back with minimal loss.
    /// </summary>
    [TestFixture]
    [Category("UnitTest")]
    public class VersionTransformationTests
    {
        [Test]
        [TestCase("gdst_1_2_feedmill_commissioning.jsonld")]
        public void GDST_1_2_FeedMillCommissioning_RoundTrip(string file)
        {
            // read object events from test data specified in the file argument
            string strEvents = OpenTraceabilityTests.ReadTestData(file);

            // deserialize object events into C# models
            EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(strEvents);

            // serialize C# models into json
            string jsonAfter = OpenTraceabilityMappers.EPCISDocument.JSON.Map(doc);

            // compare the original json with the json after round trip
            OpenTraceabilityTests.CompareJSON(strEvents, jsonAfter);
        }
    }
}
