using Newtonsoft.Json.Linq;
using OpenTraceability.Mappers;
using OpenTraceability.Mappers.EPCIS;
using OpenTraceability.Mappers.EPCIS.JSON;
using OpenTraceability.Mappers.EPCIS.XML;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Events.KDEs;
using System.Xml.Linq;

namespace OpenTraceability.Tests.Events
{
    /// <summary>
    /// This is a test class for handling the basic building and using of events.
    /// </summary>
    [TestFixture]
    public class EPCISDocumentTests
    {
        /// <summary>
        /// Tests the building of an object event and manipulating it in C#.
        /// </summary>
        [Test]
        [TestCase("object_event_all_possible_fields.xml")]
        [TestCase("aggregation_event_all_possible_fields.xml")]
        [TestCase("association_event_all_possible_fields.xml")]
        [TestCase("transformation_event_all_possible_fields.xml")]
        public void XML(string file)
        {
            // initialize the libraries.
            OpenTraceability.Initialize();

            // read object events from test data specified in the file argument
            string xmlObjectEvents = OpenTraceabilityTests.ReadTestData(file);

            // deserialize object events into C# models
            EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.XML.Map(xmlObjectEvents);

            // serialize C# models into xml
            string xmlObjectEventsAfter = OpenTraceabilityMappers.EPCISDocument.XML.Map(doc); 

            // check that the XMLs match
            OpenTraceabilityTests.CompareXML(xmlObjectEvents, xmlObjectEventsAfter);
        }

        [Test]
        [TestCase("EPCISDocument_objectevents_complete.jsonld")]
        public void JSONLD(string file)
        {
            // initialize the libraries.
            OpenTraceability.Initialize();

            // read object events from test data specified in the file argument
            string strEvents = OpenTraceabilityTests.ReadTestData(file);

            // deserialize object events into C# models
            EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(strEvents);

            // serialize C# models into xml
            string strEventsAfter = OpenTraceabilityMappers.EPCISDocument.JSON.Map(doc);

            // we need to normalize the JSON-LD before we compare it first...
            strEvents = OpenTraceabilityJsonLDMapper.NormalizeEPCISJsonLD(strEvents);

            OpenTraceabilityTests.CompareJSON(strEvents, strEventsAfter);
        }
    }
}