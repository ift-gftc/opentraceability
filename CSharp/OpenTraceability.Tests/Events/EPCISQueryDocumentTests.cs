using OpenTraceability.GDST;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;

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
        [TestCase("gdst_extensions_02.xml")]
        public void XML(string file)
        {
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
        [TestCase("querydoc_example02.xml")]
        public void Read_Crazy_XML(string file)
        {
            // read object events from test data specified in the file argument
            string xmlObjectEvents = OpenTraceabilityTests.ReadTestData(file);

            // deserialize object events into C# models
            EPCISQueryDocument doc = OpenTraceabilityMappers.EPCISQueryDocument.XML.Map(xmlObjectEvents, false);
        }

        [Test]
        [TestCase("gdst_extensions_01.xml")]
        public void XML_1_2__to__JSON_LD(string file)
        {
            // read object events from test data specified in the file argument
            string stringXmlEvents = OpenTraceabilityTests.ReadTestData(file);

            // deserialize object events into C# models
            EPCISQueryDocument doc = OpenTraceabilityMappers.EPCISQueryDocument.XML.Map(stringXmlEvents);
            doc.Header = null;
            doc.EPCISVersion = EPCISVersion.V2;

            // convert them into JSON-LD
            string jsonLD = OpenTraceabilityMappers.EPCISQueryDocument.JSON.Map(doc);

            // convert back into EPCIS Query Document
            EPCISQueryDocument docAfter = OpenTraceabilityMappers.EPCISQueryDocument.JSON.Map(jsonLD);
            docAfter.Header = null;

            // convert back into XML 1.2
            docAfter.EPCISVersion = EPCISVersion.V1;
            string xmlAfter = OpenTraceabilityMappers.EPCISQueryDocument.XML.Map(docAfter);

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
        [TestCase("EPCISQueryDocument.jsonld")]
        public void JSONLD(string file)
        {
            // read object events from test data specified in the file argument
            string json = OpenTraceabilityTests.ReadTestData(file);

            // deserialize object events into C# models
            EPCISQueryDocument doc = OpenTraceabilityMappers.EPCISQueryDocument.JSON.Map(json);

            // serialize C# models into xml
            string jsonAfter = OpenTraceabilityMappers.EPCISQueryDocument.JSON.Map(doc);

            OpenTraceabilityTests.CompareJSON(json, jsonAfter);
        }
    }
}