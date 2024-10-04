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

            // test dummy header
            doc.Header = Models.Common.StandardBusinessDocumentHeader.DummyHeader;
            xmlObjectEventsAfter = OpenTraceabilityMappers.EPCISQueryDocument.XML.Map(doc);
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
            docAfter.Header = Models.Common.StandardBusinessDocumentHeader.DummyHeader;

            // convert back into XML 1.2
            docAfter.EPCISVersion = EPCISVersion.V1;
            string xmlAfter = OpenTraceabilityMappers.EPCISQueryDocument.XML.Map(docAfter);

            // map the XML back into a document
            var finalDoc = OpenTraceabilityMappers.EPCISQueryDocument.XML.Map(xmlAfter);
            finalDoc.Header = Models.Common.StandardBusinessDocumentHeader.DummyHeader;

            xmlAfter = OpenTraceabilityMappers.EPCISQueryDocument.XML.Map(finalDoc);

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
        [TestCase("EPCISQueryDocument.GDST.jsonld")]
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

        [Test]
        [TestCase("Example_9.6.2-ObjectEvent.jsonld", "Failed to parse json from string. Expected type=EPCISQueryDocument, actual type=EPCISDocument")]
        public void FailSchema(string file, string expectedExceptionMsg)
        {
            try
            {
                // read object events from test data specified in the file argument
                string json = OpenTraceabilityTests.ReadTestData(file);

                // deserialize object events into C# models
                EPCISQueryDocument doc = OpenTraceabilityMappers.EPCISQueryDocument.JSON.Map(json);

                // serialize C# models into xml
                string jsonAfter = OpenTraceabilityMappers.EPCISQueryDocument.JSON.Map(doc);

                OpenTraceabilityTests.CompareJSON(json, jsonAfter);

                Assert.Fail("Should fail the schema check.");
            }
            catch (Exception ex)
            {
                Assert.That(ex.Message, Is.EqualTo(expectedExceptionMsg));
            }
        }

        [Test]
        [TestCase("EPCISQueryDocument_with_masterdata.jsonld")]
        public void EPCISQueryDoc_Json_WithMasterData(string file)
        {
            // read object events from test data specified in the file argument
            string json = OpenTraceabilityTests.ReadTestData(file);

            // deserialize object events into C# models
            EPCISQueryDocument doc = OpenTraceabilityMappers.EPCISQueryDocument.JSON.Map(json);

            // assert that we have master data
            Assert.IsTrue(doc.MasterData.Count > 0);

            // serialize C# models into xml
            string jsonAfter = OpenTraceabilityMappers.EPCISQueryDocument.JSON.Map(doc);

            OpenTraceabilityTests.CompareJSON(json, jsonAfter);
        }

        [Test]
        [TestCase("EPCISQUERYDOCUMENT_with_errorDeclarations.jsonld")]
        [TestCase("EPCISQUERYDOCUMENT_advanced_1.jsonld")]
        [TestCase("EPCISQUERYDOCUMENT_advanced_2.jsonld")]
        [TestCase("EPCISQUERYDOCUMENT_advanced_3.jsonld")]
        [TestCase("EPCISQUERYDOCUMENT_advanced_4.jsonld")]
        [TestCase("EPCISQUERYDOCUMENT_advanced_5.jsonld")]
        [TestCase("EPCISQUERYDOCUMENT_advanced_7.jsonld")]
        [TestCase("EPCISQUERYDOCUMENT_advanced_8.jsonld")]
        [TestCase("EPCISQUERYDOCUMENT_advanced_9.jsonld")]
        [TestCase("EPCISQUERYDOCUMENT_advanced_10.jsonld")]
        [TestCase("EPCISQUERYDOCUMENT_advanced_11.jsonld")]
        [TestCase("EPCISQUERYDOCUMENT_advanced_12.jsonld")]
        [TestCase("EPCISQUERYDOCUMENT_advanced_13.jsonld")]
        [TestCase("EPCISQUERYDOCUMENT_advanced_14.jsonld")]
        [TestCase("EPCISQUERYDOCUMENT_advanced_15.jsonld")]
        [TestCase("EPCISQUERYDOCUMENT_advanced_16.jsonld")]
        [TestCase("EPCISQUERYDOCUMENT_advanced_17.jsonld")]
        [TestCase("EPCISQUERYDOCUMENT_advanced_18.jsonld")]
        [TestCase("EPCISQUERYDOCUMENT_advanced_19.jsonld")]
        public void EPCISQueryDoc_ErrorDeclarations(string file)
        {
            // read object events from test data specified in the file argument
            string json = OpenTraceabilityTests.ReadTestData(file);

            // deserialize object events into C# models
            EPCISQueryDocument doc = OpenTraceabilityMappers.EPCISQueryDocument.JSON.Map(json);

            // map the document to xml
            string xml = OpenTraceabilityMappers.EPCISQueryDocument.XML.Map(doc);
            Assert.IsFalse(string.IsNullOrEmpty(xml));

            EPCISQueryDocument docAfter = OpenTraceabilityMappers.EPCISQueryDocument.XML.Map(xml);
            Assert.IsNotNull(docAfter);

            string jsonAfter = OpenTraceabilityMappers.EPCISQueryDocument.JSON.Map(docAfter);
            Assert.IsFalse(string.IsNullOrEmpty(jsonAfter));
        }
    }
}