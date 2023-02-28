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
        [TestCase("aggregation_event_all_possible_fields.jsonld")]
        [TestCase("AssociationEvent-a.jsonld")]
        [TestCase("AssociationEvent-b.jsonld")]
        [TestCase("AssociationEvent-c.jsonld")]
        [TestCase("AssociationEvent-d.jsonld")]
        [TestCase("AssociationEvent-e.jsonld")]
        [TestCase("AssociationEvent-f.jsonld")]
        [TestCase("AssociationEvent-g.jsonld")]
        [TestCase("AssociationEvent-h.jsonld")]
        [TestCase("association_event_all_possible_fields.jsonld")]
        [TestCase("EPCISDocument_objectevents_complete.jsonld")]
        [TestCase("ErrorDeclarationAndCorrectiveEvent.jsonld")]
        [TestCase("Example-TransactionEvents-2020_07_03y.jsonld")]
        [TestCase("Example-Type-sourceOrDestination,measurement,bizTransaction.jsonld")]
        [TestCase("Example_9.6.1-ObjectEvent-with-error-declaration.jsonld")]
        [TestCase("Example_9.6.1-ObjectEvent-with-pseudo-SBDH-headers.jsonld")]
        [TestCase("Example_9.6.1-ObjectEvent.jsonld")]
        [TestCase("Example_9.6.1-ObjectEventWithDigitalLink.jsonld")]
        [TestCase("Example_9.6.1-with-comment.jsonld")]
        [TestCase("Example_9.6.2-ObjectEvent.jsonld")]
        [TestCase("Example_9.6.2-ObjectEventWithDigitalLink.jsonld")]
        [TestCase("Example_9.6.3-AggregationEvent.jsonld")]
        [TestCase("Example_9.6.3-AggregationEventWithDigitalLink.jsonld")]
        [TestCase("Example_9.6.4-TransformationEvent.jsonld")]
        [TestCase("Example_9.6.4-TransformationEventWithDigitalLink.jsonld")]
        [TestCase("object_event_all_possible_fields.jsonld")]
        [TestCase("PersistentDisposition-example.jsonld")]
        [TestCase("SensorDataExample1.jsonld")]
        [TestCase("SensorDataExample10.jsonld")]
        //[TestCase("SensorDataExample11.jsonld")]
        [TestCase("SensorDataExample12.jsonld")]
        [TestCase("SensorDataExample13.jsonld")]
        [TestCase("SensorDataExample14.jsonld")]
        [TestCase("SensorDataExample15.jsonld")]
        [TestCase("SensorDataExample16.jsonld")]
        [TestCase("SensorDataExample17.jsonld")]
        [TestCase("SensorDataExample1b.jsonld")]
        [TestCase("SensorDataExample2.jsonld")]
        [TestCase("SensorDataExample3.jsonld")]
        [TestCase("SensorDataExample4.jsonld")]
        [TestCase("SensorDataExample5.jsonld")]
        [TestCase("SensorDataExample6.jsonld")]
        [TestCase("SensorDataExample7.jsonld")]
        //[TestCase("SensorDataExample8.jsonld")]
        [TestCase("SensorDataExample9.jsonld")]
        [TestCase("transaction_event_all_possible_fields.jsonld")]
        [TestCase("transformation_event_all_possible_fields.jsonld")]
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

            OpenTraceabilityTests.CompareJSON(strEvents, strEventsAfter);
        }
    }
}