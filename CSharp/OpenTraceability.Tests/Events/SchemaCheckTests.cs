using OpenTraceability.Mappers.EPCIS.XML;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;

namespace OpenTraceability.Tests.Events
{
    [TestFixture]
    public class SchemaCheckTests
    {
        [Test]
        [TestCase("querydoc_fail_schemacheck.xml", false)]
        public void EPCISQueryDocument_XML_1_2(string file, bool pass)
        {
            // read object events from test data specified in the file argument
            string xmlObjectEvents = OpenTraceabilityTests.ReadTestData(file);

            XDocument xDoc = XDocument.Parse(xmlObjectEvents);
            try
            {
                EPCISDocumentBaseXMLMapper.ValidateEPCISQueryDocumentSchema(xDoc, Models.Events.EPCISVersion.V1);
                Assert.That(pass, Is.True);
            }
            catch (Exception)
            {
                Assert.That(pass, Is.False);
            }
        }
    }
}