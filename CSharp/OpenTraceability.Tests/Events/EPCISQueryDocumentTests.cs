using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Tests.Events
{
    [TestFixture]
    public class EPCISQueryDocumentTests
    {
        /// <summary>
        /// Tests the building of an object event and manipulating it in C#.
        /// </summary>
        [Test]
        [TestCase("querydoc_example01.xml")]
        public void XML(string file)
        {
            // initialize the libraries.
            OpenTraceability.Initialize();

            // read object events from test data specified in the file argument
            string xmlObjectEvents = OpenTraceabilityTests.ReadTestData(file);

            // deserialize object events into C# models
            EPCISQueryDocument doc = OpenTraceabilityMappers.EPCISQueryDocument.XML.Map(xmlObjectEvents);

            // serialize C# models into xml
            string xmlObjectEventsAfter = OpenTraceabilityMappers.EPCISQueryDocument.XML.Map(doc);

            // check that the XMLs match
            OpenTraceabilityTests.CompareXML(xmlObjectEvents, xmlObjectEventsAfter);
        }

        [Test]
        [TestCase("EPCISQueryDocument.jsonld")]
        public void JSONLD(string file)
        {
            // initialize the libraries.
            OpenTraceability.Initialize();

            // read object events from test data specified in the file argument
            string json = OpenTraceabilityTests.ReadTestData(file);

            // deserialize object events into C# models
            EPCISQueryDocument doc = OpenTraceabilityMappers.EPCISQueryDocument.JSON.Map(json);

            // serialize C# models into xml
            string jsonAfter = OpenTraceabilityMappers.EPCISQueryDocument.JSON.Map(doc);

            // we need to normalize the JSON-LD before we compare it first...
            json = OpenTraceabilityJsonLDMapper.NormalizeEPCISJsonLD(json);

            OpenTraceabilityTests.CompareJSON(json, jsonAfter);
        }
    }
}
