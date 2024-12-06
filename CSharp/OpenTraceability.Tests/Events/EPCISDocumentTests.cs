using Newtonsoft.Json.Linq;
using OpenTraceability.Mappers;
using OpenTraceability.Mappers.EPCIS;
using OpenTraceability.Mappers.EPCIS.JSON;
using OpenTraceability.Mappers.EPCIS.XML;
using OpenTraceability.Models.Common;
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
        [TestCase("gdst_extensions_03.xml")]
        public void XML_1_2__to__JSON_LD(string file)
        {
            OpenTraceability.GDST.Setup.Initialize();

            // read object events from test data specified in the file argument
            string stringXmlEvents = OpenTraceabilityTests.ReadTestData(file);

            // deserialize object events into C# models
            EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.XML.Map(stringXmlEvents);
            doc.Header = null;
            doc.EPCISVersion = EPCISVersion.V2;

            // convert them into JSON-LD
            string jsonLD = OpenTraceabilityMappers.EPCISDocument.JSON.Map(doc);

            // convert back into EPCIS Query Document
            EPCISDocument docAfter = OpenTraceabilityMappers.EPCISDocument.JSON.Map(jsonLD);
            docAfter.Header = StandardBusinessDocumentHeader.DummyHeader;

            // convert back into XML 1.2
            docAfter.EPCISVersion = EPCISVersion.V1;
            string xmlAfter = OpenTraceabilityMappers.EPCISDocument.XML.Map(docAfter);

            // map the XML back into a document
            var finalDoc = OpenTraceabilityMappers.EPCISDocument.XML.Map(xmlAfter);

            xmlAfter = OpenTraceabilityMappers.EPCISDocument.XML.Map(finalDoc);

            // change all the https://ref.gs1.org/cbv/ to "urn:epcglobal:cbv:
            xmlAfter = xmlAfter.Replace("https://ref.gs1.org/cbv/Disp-", "urn:epcglobal:cbv:disp:");
            xmlAfter = xmlAfter.Replace("https://ref.gs1.org/cbv/BizStep-", "urn:epcglobal:cbv:bizstep:");
            xmlAfter = xmlAfter.Replace("https://ref.gs1.org/cbv/SDT-", "urn:epcglobal:cbv:sdt:");

            // compare the <EPCISBody> element
            XElement x1 = XElement.Parse(stringXmlEvents).Element("EPCISBody") ?? throw new Exception("failed to grab EPCISBody element from the XML for stringXmlEvents=" + stringXmlEvents);
            XElement x2 = XElement.Parse(xmlAfter).Element("EPCISBody") ?? throw new Exception("failed to grab EPCISBody element from the XML for xmlAfter=" + xmlAfter);

            OpenTraceabilityTests.CompareXML(x1.ToString(), x2.ToString());
        }

        [Test]
        [TestCase("gdst_data_withmasterdata.jsonld")]
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
        [TestCase("wholechainbug01.jsonld")]
        [TestCase("farm_harvest_event_object.jsonld")]
        public void JSONLD(string file)
        {
            // read object events from test data specified in the file argument
            string strEvents = OpenTraceabilityTests.ReadTestData(file);

            // deserialize object events into C# models
            EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(strEvents);

            // serialize C# models into xml
            string strEventsAfter = OpenTraceabilityMappers.EPCISDocument.JSON.Map(doc);

            OpenTraceabilityTests.CompareJSON(strEvents, strEventsAfter);
        }

        [Test]
        public void TestHarness()
        {
            // read object events from test data specified in the file argument
            string strEvents = OpenTraceabilityTests.ReadTestData("cap_tool_events.jsonld");

            // read object events from test data specified in the file argument
            string strAfterEvents = OpenTraceabilityTests.ReadTestData("all_wholechain_events.jsonld");

            // deserialize object events into C# models
            EPCISQueryDocument doc = OpenTraceabilityMappers.EPCISQueryDocument.JSON.Map(strEvents);

            // deserialize object events into C# models
            EPCISQueryDocument afterDoc = OpenTraceabilityMappers.EPCISQueryDocument.JSON.Map(strEvents);

            List<string> beforeEventIDs = doc.Events.Select(e => e.EventID.ToString()).ToList();
            List<string> afterEventIDs = afterDoc.Events.Select(e => e.EventID.ToString()).ToList();

            List<string> missing = beforeEventIDs.Except(afterEventIDs).ToList();
        }
    }
}