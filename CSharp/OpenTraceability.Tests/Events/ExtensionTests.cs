using OpenTraceability.GDST;
using OpenTraceability.GDST.Events;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Tests.Events
{
    internal class ExtensionTests
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
            OpenTraceabilityGDST.Initialize();

            // read object events from test data specified in the file argument
            string xmlObjectEvents = OpenTraceabilityTests.ReadTestData(file);

            // deserialize object events into C# models
            EPCISQueryDocument doc = OpenTraceabilityMappers.EPCISQueryDocument.XML.Map(xmlObjectEvents);

            // check that we find an GDST fishing event...
            Assert.That(doc.Events.Exists(e => e is GDSTFishingEvent), Is.True);

            GDSTFishingEvent fishingEvent = (GDSTFishingEvent)doc.Events.First(e => e is GDSTFishingEvent);
            Assert.That(fishingEvent.ILMD.VesselCatchInformationList?.Vessels.Count() > 0, Is.True);
        }
    }
}
