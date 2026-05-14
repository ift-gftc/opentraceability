using OpenTraceability.GDST;
using OpenTraceability.GDST.Events;
using OpenTraceability.GDST.MasterData;
using OpenTraceability.Interfaces;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility;

namespace OpenTraceability.Tests.Events
{
    [Category("UnitTest")]
    internal class GDSTEventModelTests
    {
        [Test]
        public void ConstructorDefaults()
        {
            AssertCombination(new GDSTCommissionEvent(), EventType.ObjectEvent, EventAction.ADD, "urn:epcglobal:cbv:bizstep:commissioning", "active");
            AssertCombination(new GDSTAggregationEvent(), EventType.AggregationEvent, EventAction.ADD, "urn:epcglobal:cbv:bizstep:packing", "active");
            AssertCombination(new GDSTDisaggregationEvent(), EventType.AggregationEvent, EventAction.DELETE, "urn:epcglobal:cbv:bizstep:unpacking", "inactive");
            AssertCombination(new GDSTShippingEvent(), EventType.ObjectEvent, EventAction.OBSERVE, "urn:epcglobal:cbv:bizstep:shipping", "in_transit");
            AssertCombination(new GDSTReceivingEvent(), EventType.ObjectEvent, EventAction.OBSERVE, "urn:epcglobal:cbv:bizstep:receiving", "arrived");
            AssertCombination(new GDSTTransformationEvent(), EventType.TransformationEvent, null, "urn:epcglobal:cbv:bizstep:commissioning", "active");
            AssertCombination(new GDSTDecommissionEvent(), EventType.ObjectEvent, EventAction.DELETE, "urn:epcglobal:cbv:bizstep:destroying", null);
        }

        [Test]
        public void JSONLD_ProfilesToCombinationClass()
        {
            Setup.Initialize();

            string json = OpenTraceabilityTests.ReadTestData("gdst_data_withmasterdata.jsonld");
            EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(json);

            Assert.That(doc.Events.Any(e => e is GDSTShippingEvent), Is.True);
            Assert.That(doc.Events.Any(e => e is GDSTReceivingEvent), Is.True);
            Assert.That(doc.Events.Any(e => e is GDSTTransformationEvent), Is.True);
        }

        [Test]
        public void MasterDataClassification_JSONLD_RoundTrips()
        {
            var tradeItem = new GDSTTradeItem
            {
                GTIN = new GTIN("urn:gdst:example.org:product:class:feedmill.1"),
                ProductClassification =
                {
                    new GDSTClassification { Type = "GDST", Value = "Feed" }
                }
            };

            string json = OpenTraceabilityMappers.MasterData.GS1WebVocab.Map(tradeItem);
            var mapped = (GDSTTradeItem)OpenTraceabilityMappers.MasterData.GS1WebVocab.Map<GDSTTradeItem>(json);

            Assert.That(mapped.ProductClassification.Any(c => c.Value == "Feed"), Is.True);
        }

        [Test]
        public void SemanticProfileResolver_UsesMasterData()
        {
            EPCISDocument doc = BuildProfileDocument();

            Assert.That(GDSTEventProfileResolver.Resolve(doc.Events[0], doc), Is.EqualTo(GDSTEventProfile.Fishing));
            Assert.That(GDSTEventProfileResolver.Resolve(doc.Events[1], doc), Is.EqualTo(GDSTEventProfile.TransshipmentShipping));
            Assert.That(GDSTEventProfileResolver.Resolve(doc.Events[2], doc), Is.EqualTo(GDSTEventProfile.Landing));
            Assert.That(GDSTEventProfileResolver.Resolve(doc.Events[3], doc), Is.EqualTo(GDSTEventProfile.FeedProcessing));
        }

        private static EPCISDocument BuildProfileDocument()
        {
            const string wildProduct = "urn:gdst:example.org:product:class:fisherman01.tuna";
            const string wildProductLotOne = "urn:gdst:example.org:product:lot:class:fisherman01.tuna.lot1";
            const string wildProductLotTwo = "urn:gdst:example.org:product:lot:class:fisherman01.tuna.lot2";
            const string feedProduct = "urn:gdst:example.org:product:class:feedmill.1";
            const string feedProductLot = "urn:gdst:example.org:product:lot:class:feedmill.1.lot3";
            const string vesselOne = "urn:gdst:example.org:location:loc:vessel.1";
            const string vesselTwo = "urn:gdst:example.org:location:loc:vessel.2";
            const string landFacility = "urn:gdst:example.org:location:loc:processor.plant1";

            var doc = new EPCISDocument();
            doc.MasterData.Add(new GDSTTradeItem
            {
                GTIN = new GTIN(wildProduct),
                ProductClassification =
                {
                    new GDSTClassification { Type = "GDST", Value = "WildCaught" }
                }
            });
            doc.MasterData.Add(new GDSTTradeItem
            {
                GTIN = new GTIN(feedProduct),
                ProductClassification =
                {
                    new GDSTClassification { Type = "GDST", Value = "Feed" }
                }
            });
            doc.MasterData.Add(BuildLocation(vesselOne, "Vessel"));
            doc.MasterData.Add(BuildLocation(vesselTwo, "Vessel"));
            doc.MasterData.Add(BuildLocation(landFacility, "Land Facility"));

            var fishing = new GDSTCommissionEvent();
            fishing.AddProduct(new EventProduct(new EPC(wildProductLotOne))
            {
                Type = EventProductType.Reference,
                Quantity = new Measurement(10, "KGM")
            });
            doc.Events.Add(fishing);

            var transshipmentShipping = new GDSTShippingEvent();
            transshipmentShipping.SourceList.Add(BuildSource(vesselOne));
            transshipmentShipping.DestinationList.Add(BuildDestination(vesselTwo));
            doc.Events.Add(transshipmentShipping);

            var landing = new GDSTReceivingEvent();
            landing.SourceList.Add(BuildSource(vesselOne));
            landing.DestinationList.Add(BuildDestination(landFacility));
            doc.Events.Add(landing);

            var feedProcessing = new GDSTTransformationEvent();
            feedProcessing.AddProduct(new EventProduct(new EPC(wildProductLotTwo))
            {
                Type = EventProductType.Input,
                Quantity = new Measurement(5, "KGM")
            });
            feedProcessing.AddProduct(new EventProduct(new EPC(feedProductLot))
            {
                Type = EventProductType.Output,
                Quantity = new Measurement(5, "KGM")
            });
            doc.Events.Add(feedProcessing);

            return doc;
        }

        private static GDSTLocation BuildLocation(string id, string classification)
        {
            return new GDSTLocation
            {
                GLN = new GLN(id),
                LocationClassification =
                {
                    new GDSTClassification { Type = "GDST", Value = classification }
                }
            };
        }

        private static EventSource BuildSource(string location)
        {
            return new EventSource
            {
                ParsedType = EventSourceType.Location,
                Value = location
            };
        }

        private static EventDestination BuildDestination(string location)
        {
            return new EventDestination
            {
                ParsedType = EventDestinationType.Location,
                Value = location
            };
        }

        private static void AssertCombination(IEvent evt, EventType eventType, EventAction? action, string businessStep, string? disposition)
        {
            Assert.That(evt.EventType, Is.EqualTo(eventType));
            Assert.That(evt.Action, Is.EqualTo(action));
            Assert.That(evt.BusinessStep.ToString(), Is.EqualTo(businessStep));
            Assert.That(evt.Disposition?.ToString(), Is.EqualTo(disposition));
        }
    }
}
