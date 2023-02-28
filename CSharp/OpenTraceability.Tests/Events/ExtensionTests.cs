using OpenTraceability.GDST;
using OpenTraceability.GDST.Events;
using OpenTraceability.GDST.MasterData;
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
        [TestCase("gdst_extensions_01.xml")]
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

            // check other event types were profiled correctly
            GDSTProcessingEvent processingEvent = (GDSTProcessingEvent)doc.Events.First(e => e is GDSTProcessingEvent);
            GDSTFeedmillObjectEvent feedmillEvent = (GDSTFeedmillObjectEvent)doc.Events.First(e => e is GDSTFeedmillObjectEvent);

            // check master data was profiled correctly
            Assert.That(doc.GetMasterData<GDSTLocation>().Count > 0, Is.True);
        }
    }
}
