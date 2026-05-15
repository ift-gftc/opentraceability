using OpenTraceability.GDST;
using OpenTraceability.GDST.Events;
using OpenTraceability.GDST.Events.KDEs;
using OpenTraceability.GDST.MasterData;
using OpenTraceability.Interfaces;
using OpenTraceability.Mappers;
using OpenTraceability.Models.Common;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Events.KDEs;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility;

namespace OpenTraceability.Tests.Events
{
    [Category("UnitTest")]
    internal class GDSTEventModelTests
    {
        private const string HarvestCert = "urn:gdst:certType:harvestCert";
        private const string HumanPolicy = "urn:gdst:certType:humanPolicy";
        private const string FishingAuth = "urn:gdst:certType:fishingAuth";
        private const string LegalAuth = "urn:gdst:certType:legalAuth";
        private const string HarvestCoC = "urn:gdst:certType:harvestCoC";
        private const string TransshipmentAuth = "urn:gdst:certType:transshipmentAuth";
        private const string LandingAuth = "urn:gdst:certType:landingAuth";
        private const string ProcessorLicense = "urn:gdst:certType:processorLicense";

        private const string InformationProvider = "urn:gdst:test:party:information-provider";
        private const string ProductOwner = "urn:gdst:test:party:product-owner";
        private const string DestinationOwner = "urn:gdst:test:party:destination-owner";

        private const string VesselOne = "urn:gdst:test:location:loc:vessel.1";
        private const string VesselTwo = "urn:gdst:test:location:loc:vessel.2";
        private const string LandFacilityOne = "urn:gdst:test:location:loc:land.1";
        private const string LandFacilityTwo = "urn:gdst:test:location:loc:land.2";
        private const string Hatchery = "urn:gdst:test:location:loc:hatchery.1";
        private const string Processor = "urn:gdst:test:location:loc:processor.1";

        private const string WildProduct = "urn:gdst:test:product:class:wild.tuna";
        private const string DevelopingProduct = "urn:gdst:test:product:class:developing.salmon";
        private const string FeedProduct = "urn:gdst:test:product:class:feed.formula";
        private const string MatureProduct = "urn:gdst:test:product:class:mature.salmon";
        private const string ProcessedProduct = "urn:gdst:test:product:class:processed.fillet";
        private const string CommingledProduct = "urn:gdst:test:product:class:commingled.tuna";
        private const string GenericProduct = "urn:gdst:test:product:class:generic.product";

        [Test]
        public void ConstructorDefaults()
        {
            AssertCombination(new GDSTCommissionEvent(), EventType.ObjectEvent, EventAction.ADD, "urn:epcglobal:cbv:bizstep:commissioning");
            AssertCombination(new GDSTAggregationEvent(), EventType.AggregationEvent, EventAction.ADD, "urn:epcglobal:cbv:bizstep:packing");
            AssertCombination(new GDSTDisaggregationEvent(), EventType.AggregationEvent, EventAction.DELETE, "urn:epcglobal:cbv:bizstep:unpacking");
            AssertCombination(new GDSTShippingEvent(), EventType.ObjectEvent, EventAction.OBSERVE, "urn:epcglobal:cbv:bizstep:shipping");
            AssertCombination(new GDSTReceivingEvent(), EventType.ObjectEvent, EventAction.OBSERVE, "urn:epcglobal:cbv:bizstep:receiving");
            AssertCombination(new GDSTTransformationEvent(), EventType.TransformationEvent, null, "urn:epcglobal:cbv:bizstep:commissioning");
            AssertCombination(new GDSTDecommissionEvent(), EventType.ObjectEvent, EventAction.DELETE, "urn:epcglobal:cbv:bizstep:destroying");
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

        [Test]
        [TestCaseSource(nameof(ProfileRoundTripCases))]
        public void GDSTProfiles_JSONLD_RoundTripWithoutDataLoss(ProfileRoundTripCase testCase)
        {
            Setup.Initialize();

            EPCISDocument sourceDoc = testCase.BuildDocument();
            IEvent sourceEvent = sourceDoc.Events.Single();
            AssertProfileCase(testCase, sourceDoc, sourceEvent);

            string json = OpenTraceabilityMappers.EPCISDocument.JSON.Map(sourceDoc);
            EPCISDocument mappedDoc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(json);
            string jsonAfter = OpenTraceabilityMappers.EPCISDocument.JSON.Map(mappedDoc);

            OpenTraceabilityTests.CompareJSON(json, jsonAfter);

            Assert.That(mappedDoc.Events, Has.Count.EqualTo(1));
            IEvent mappedEvent = mappedDoc.Events.Single();
            AssertProfileCase(testCase, mappedDoc, mappedEvent);
            AssertRoundTrippedEvent(sourceEvent, mappedEvent);
            AssertMasterDataRoundTripped(sourceDoc, mappedDoc);
            testCase.AssertProfileProperties(mappedEvent);
        }

        [Test]
        public void GDSTCommissionEvent_WithoutClassification_RoundTripsAsUnknownProfile()
        {
            Setup.Initialize();

            ProfileRoundTripCase testCase = BuildUnknownCommissionCase();
            EPCISDocument sourceDoc = testCase.BuildDocument();
            string json = OpenTraceabilityMappers.EPCISDocument.JSON.Map(sourceDoc);
            EPCISDocument mappedDoc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(json);
            string jsonAfter = OpenTraceabilityMappers.EPCISDocument.JSON.Map(mappedDoc);

            OpenTraceabilityTests.CompareJSON(json, jsonAfter);
            AssertProfileCase(testCase, mappedDoc, mappedDoc.Events.Single());
        }

        [Test]
        public void GDSTCertificates_JSONLD_RoundTripMultipleTypes()
        {
            Setup.Initialize();

            string[] certificateTypes =
            {
                HarvestCert,
                HumanPolicy,
                FishingAuth,
                LegalAuth,
                HarvestCoC,
                TransshipmentAuth,
                LandingAuth,
                ProcessorLicense
            };

            EPCISDocument sourceDoc = BuildBaseDocument();
            var shipping = new GDSTShippingEvent();
            ApplyCommonFields(shipping, "certificate-regression", LandFacilityOne);
            shipping.CertificationList = BuildCertificationList(certificateTypes);
            shipping.AddProduct(BuildProduct(GenericProduct, "certificate-regression", EventProductType.Reference, 1));
            shipping.SourceList.Add(BuildSource(EventSourceType.Location, LandFacilityOne));
            shipping.DestinationList.Add(BuildDestination(EventDestinationType.Location, LandFacilityTwo));
            sourceDoc.Events.Add(shipping);

            string json = OpenTraceabilityMappers.EPCISDocument.JSON.Map(sourceDoc);
            EPCISDocument mappedDoc = OpenTraceabilityMappers.EPCISDocument.JSON.Map(json);
            string jsonAfter = OpenTraceabilityMappers.EPCISDocument.JSON.Map(mappedDoc);

            OpenTraceabilityTests.CompareJSON(json, jsonAfter);
            AssertCertificates(mappedDoc.Events.Single(), certificateTypes);
        }

        private static IEnumerable<TestCaseData> ProfileRoundTripCases()
        {
            yield return new TestCaseData(BuildFishingCase()).SetName("GDST profile Fishing JSON-LD round trip");
            yield return new TestCaseData(BuildImmatureHarvestCase()).SetName("GDST profile ImmatureHarvest JSON-LD round trip");
            yield return new TestCaseData(BuildAggregationCase()).SetName("GDST profile Aggregation JSON-LD round trip");
            yield return new TestCaseData(BuildDisaggregationCase()).SetName("GDST profile Disaggregation JSON-LD round trip");
            yield return new TestCaseData(BuildShippingCase()).SetName("GDST profile Shipping JSON-LD round trip");
            yield return new TestCaseData(BuildTransshipmentShippingCase()).SetName("GDST profile TransshipmentShipping JSON-LD round trip");
            yield return new TestCaseData(BuildReceivingCase()).SetName("GDST profile Receiving JSON-LD round trip");
            yield return new TestCaseData(BuildTransshipmentReceivingCase()).SetName("GDST profile TransshipmentReceiving JSON-LD round trip");
            yield return new TestCaseData(BuildLandingCase()).SetName("GDST profile Landing JSON-LD round trip");
            yield return new TestCaseData(BuildComminglingCase()).SetName("GDST profile Commingling JSON-LD round trip");
            yield return new TestCaseData(BuildProcessingCase()).SetName("GDST profile Processing JSON-LD round trip");
            yield return new TestCaseData(BuildOnVesselProcessingCase()).SetName("GDST profile OnVesselProcessing JSON-LD round trip");
            yield return new TestCaseData(BuildFeedProcessingCase()).SetName("GDST profile FeedProcessing JSON-LD round trip");
            yield return new TestCaseData(BuildMatureHarvestCase()).SetName("GDST profile MatureHarvest JSON-LD round trip");
            yield return new TestCaseData(BuildDecommissionCase()).SetName("GDST profile Decommission JSON-LD round trip");
        }

        private static ProfileRoundTripCase BuildFishingCase()
        {
            return new ProfileRoundTripCase(
                nameof(GDSTEventProfile.Fishing),
                typeof(GDSTCommissionEvent),
                GDSTEventProfile.Fishing,
                new[] { HarvestCert, HumanPolicy, FishingAuth },
                () =>
                {
                    EPCISDocument doc = BuildBaseDocument();
                    var evt = new GDSTCommissionEvent
                    {
                        ProductOwner = new PGLN(ProductOwner),
                        HumanWelfarePolicy = "3P audit",
                        ILMD = BuildWildCatchILMD()
                    };
                    ApplyCommonFields(evt, "fishing", VesselOne);
                    evt.CertificationList = BuildCertificationList(HarvestCert, HumanPolicy, FishingAuth);
                    evt.AddProduct(BuildProduct(WildProduct, "fishing-lot", EventProductType.Reference, 125));
                    doc.Events.Add(evt);
                    return doc;
                },
                evt =>
                {
                    var gdstEvent = AssertEventType<GDSTCommissionEvent>(evt);
                    Assert.That(gdstEvent.HumanWelfarePolicy, Is.EqualTo("3P audit"));
                    Assert.That(gdstEvent.ProductOwner?.ToString(), Is.EqualTo(ProductOwner));
                    AssertWildCatchILMD(gdstEvent.ILMD);
                });
        }

        private static ProfileRoundTripCase BuildImmatureHarvestCase()
        {
            return new ProfileRoundTripCase(
                nameof(GDSTEventProfile.ImmatureHarvest),
                typeof(GDSTCommissionEvent),
                GDSTEventProfile.ImmatureHarvest,
                new[] { HarvestCert, HumanPolicy, LegalAuth },
                () =>
                {
                    EPCISDocument doc = BuildBaseDocument();
                    var evt = new GDSTCommissionEvent
                    {
                        ProductOwner = new PGLN(ProductOwner),
                        HumanWelfarePolicy = "internal policy",
                        ILMD = new GDSTILMD
                        {
                            BroodstockSource = "domestic",
                            ProductionMethodForFishAndSeafoodCode = "AQUACULTURE",
                            LotNumber = "IMMATURE-LOT-1"
                        }
                    };
                    ApplyCommonFields(evt, "immature-harvest", Hatchery);
                    evt.CertificationList = BuildCertificationList(HarvestCert, HumanPolicy, LegalAuth);
                    evt.AddProduct(BuildProduct(DevelopingProduct, "immature-lot", EventProductType.Reference, 75));
                    doc.Events.Add(evt);
                    return doc;
                },
                evt =>
                {
                    var gdstEvent = AssertEventType<GDSTCommissionEvent>(evt);
                    Assert.That(gdstEvent.HumanWelfarePolicy, Is.EqualTo("internal policy"));
                    Assert.That(gdstEvent.ILMD?.BroodstockSource, Is.EqualTo("domestic"));
                    Assert.That(gdstEvent.ILMD?.ProductionMethodForFishAndSeafoodCode, Is.EqualTo("AQUACULTURE"));
                });
        }

        private static ProfileRoundTripCase BuildAggregationCase()
        {
            return new ProfileRoundTripCase(
                nameof(GDSTEventProfile.Aggregation),
                typeof(GDSTAggregationEvent),
                GDSTEventProfile.Aggregation,
                new[] { HarvestCoC },
                () =>
                {
                    EPCISDocument doc = BuildBaseDocument();
                    var evt = new GDSTAggregationEvent
                    {
                        ProductOwner = new PGLN(ProductOwner)
                    };
                    ApplyCommonFields(evt, "aggregation", Processor);
                    evt.CertificationList = BuildCertificationList(HarvestCoC);
                    evt.AddProduct(new EventProduct(new EPC("urn:epc:id:sscc:0614141.0000000010")) { Type = EventProductType.Parent });
                    evt.AddProduct(BuildProduct(WildProduct, "aggregation-child", EventProductType.Child, 25));
                    doc.Events.Add(evt);
                    return doc;
                },
                evt =>
                {
                    var gdstEvent = AssertEventType<GDSTAggregationEvent>(evt);
                    Assert.That(gdstEvent.ProductOwner?.ToString(), Is.EqualTo(ProductOwner));
                    Assert.That(gdstEvent.Products.Count(p => p.Type == EventProductType.Parent), Is.EqualTo(1));
                    Assert.That(gdstEvent.Products.Count(p => p.Type == EventProductType.Child), Is.EqualTo(1));
                });
        }

        private static ProfileRoundTripCase BuildDisaggregationCase()
        {
            return new ProfileRoundTripCase(
                nameof(GDSTEventProfile.Disaggregation),
                typeof(GDSTDisaggregationEvent),
                GDSTEventProfile.Disaggregation,
                new[] { HarvestCoC },
                () =>
                {
                    EPCISDocument doc = BuildBaseDocument();
                    var evt = new GDSTDisaggregationEvent
                    {
                        ProductOwner = new PGLN(ProductOwner)
                    };
                    ApplyCommonFields(evt, "disaggregation", Processor);
                    evt.CertificationList = BuildCertificationList(HarvestCoC);
                    evt.AddProduct(new EventProduct(new EPC("urn:epc:id:sscc:0614141.0000000011")) { Type = EventProductType.Parent });
                    evt.AddProduct(BuildProduct(WildProduct, "disaggregation-child", EventProductType.Child, 12));
                    doc.Events.Add(evt);
                    return doc;
                },
                evt =>
                {
                    var gdstEvent = AssertEventType<GDSTDisaggregationEvent>(evt);
                    Assert.That(gdstEvent.ProductOwner?.ToString(), Is.EqualTo(ProductOwner));
                    Assert.That(gdstEvent.Products.Count(p => p.Type == EventProductType.Parent), Is.EqualTo(1));
                    Assert.That(gdstEvent.Products.Count(p => p.Type == EventProductType.Child), Is.EqualTo(1));
                });
        }

        private static ProfileRoundTripCase BuildShippingCase()
        {
            return BuildShippingProfileCase(
                nameof(GDSTEventProfile.Shipping),
                GDSTEventProfile.Shipping,
                new[] { HarvestCoC },
                LandFacilityOne,
                LandFacilityTwo,
                "shipping");
        }

        private static ProfileRoundTripCase BuildTransshipmentShippingCase()
        {
            return BuildShippingProfileCase(
                nameof(GDSTEventProfile.TransshipmentShipping),
                GDSTEventProfile.TransshipmentShipping,
                new[] { HarvestCoC, HumanPolicy, TransshipmentAuth },
                VesselOne,
                VesselTwo,
                "transshipment-shipping");
        }

        private static ProfileRoundTripCase BuildReceivingCase()
        {
            return BuildReceivingProfileCase(
                nameof(GDSTEventProfile.Receiving),
                GDSTEventProfile.Receiving,
                new[] { HarvestCoC },
                LandFacilityOne,
                LandFacilityTwo,
                "receiving",
                null,
                null);
        }

        private static ProfileRoundTripCase BuildTransshipmentReceivingCase()
        {
            return BuildReceivingProfileCase(
                nameof(GDSTEventProfile.TransshipmentReceiving),
                GDSTEventProfile.TransshipmentReceiving,
                new[] { HarvestCoC, HumanPolicy, TransshipmentAuth },
                VesselOne,
                VesselTwo,
                "transshipment-receiving",
                "3P audit",
                null);
        }

        private static ProfileRoundTripCase BuildLandingCase()
        {
            return BuildReceivingProfileCase(
                nameof(GDSTEventProfile.Landing),
                GDSTEventProfile.Landing,
                new[] { HarvestCert, HumanPolicy, LandingAuth },
                VesselOne,
                LandFacilityOne,
                "landing",
                "3P audit",
                "urn:gdst:test:location:loc:port.1");
        }

        private static ProfileRoundTripCase BuildComminglingCase()
        {
            return BuildTransformationProfileCase(
                nameof(GDSTEventProfile.Commingling),
                GDSTEventProfile.Commingling,
                new[] { HarvestCoC, ProcessorLicense },
                LandFacilityOne,
                CommingledProduct,
                CommingledProduct,
                "commingling",
                null,
                null,
                null);
        }

        private static ProfileRoundTripCase BuildProcessingCase()
        {
            return BuildTransformationProfileCase(
                nameof(GDSTEventProfile.Processing),
                GDSTEventProfile.Processing,
                new[] { HarvestCoC, ProcessorLicense },
                Processor,
                WildProduct,
                ProcessedProduct,
                "processing",
                null,
                null,
                null);
        }

        private static ProfileRoundTripCase BuildOnVesselProcessingCase()
        {
            return BuildTransformationProfileCase(
                nameof(GDSTEventProfile.OnVesselProcessing),
                GDSTEventProfile.OnVesselProcessing,
                new[] { HarvestCoC, HumanPolicy, ProcessorLicense },
                VesselOne,
                WildProduct,
                ProcessedProduct,
                "on-vessel-processing",
                "3P audit",
                null,
                null);
        }

        private static ProfileRoundTripCase BuildFeedProcessingCase()
        {
            return BuildTransformationProfileCase(
                nameof(GDSTEventProfile.FeedProcessing),
                GDSTEventProfile.FeedProcessing,
                new[] { HarvestCoC, ProcessorLicense },
                Processor,
                WildProduct,
                FeedProduct,
                "feed-processing",
                null,
                "soy, insects, wild caught fish byproduct",
                null);
        }

        private static ProfileRoundTripCase BuildMatureHarvestCase()
        {
            return BuildTransformationProfileCase(
                nameof(GDSTEventProfile.MatureHarvest),
                GDSTEventProfile.MatureHarvest,
                new[] { HarvestCert, HumanPolicy, HarvestCoC, LegalAuth },
                Hatchery,
                DevelopingProduct,
                MatureProduct,
                "mature-harvest",
                "internal policy",
                null,
                "semi-intensive");
        }

        private static ProfileRoundTripCase BuildDecommissionCase()
        {
            return new ProfileRoundTripCase(
                nameof(GDSTEventProfile.Decommission),
                typeof(GDSTDecommissionEvent),
                GDSTEventProfile.Decommission,
                Array.Empty<string>(),
                () =>
                {
                    EPCISDocument doc = BuildBaseDocument();
                    var evt = new GDSTDecommissionEvent
                    {
                        ProductOwner = new PGLN(ProductOwner)
                    };
                    ApplyCommonFields(evt, "decommission", LandFacilityOne);
                    evt.AddProduct(BuildProduct(GenericProduct, "decommission-lot", EventProductType.Reference, 4));
                    doc.Events.Add(evt);
                    return doc;
                },
                evt =>
                {
                    var gdstEvent = AssertEventType<GDSTDecommissionEvent>(evt);
                    Assert.That(gdstEvent.ProductOwner?.ToString(), Is.EqualTo(ProductOwner));
                    AssertCertificates(gdstEvent, Array.Empty<string>());
                });
        }

        private static ProfileRoundTripCase BuildUnknownCommissionCase()
        {
            return new ProfileRoundTripCase(
                "UnknownCommission",
                typeof(GDSTCommissionEvent),
                GDSTEventProfile.Unknown,
                new[] { HarvestCert },
                () =>
                {
                    EPCISDocument doc = BuildBaseDocument();
                    var evt = new GDSTCommissionEvent
                    {
                        ProductOwner = new PGLN(ProductOwner)
                    };
                    ApplyCommonFields(evt, "unknown-commission", LandFacilityOne);
                    evt.CertificationList = BuildCertificationList(HarvestCert);
                    evt.AddProduct(BuildProduct(GenericProduct, "unknown-commission-lot", EventProductType.Reference, 5));
                    doc.Events.Add(evt);
                    return doc;
                },
                evt => AssertEventType<GDSTCommissionEvent>(evt));
        }

        private static ProfileRoundTripCase BuildShippingProfileCase(
            string name,
            GDSTEventProfile expectedProfile,
            string[] certificateTypes,
            string sourceLocation,
            string destinationLocation,
            string suffix)
        {
            return new ProfileRoundTripCase(
                name,
                typeof(GDSTShippingEvent),
                expectedProfile,
                certificateTypes,
                () =>
                {
                    EPCISDocument doc = BuildBaseDocument();
                    var evt = new GDSTShippingEvent();
                    ApplyCommonFields(evt, suffix, sourceLocation);
                    evt.CertificationList = BuildCertificationList(certificateTypes);
                    evt.UnloadingPort = "urn:gdst:test:location:loc:port.shipping";
                    evt.AddProduct(BuildProduct(GenericProduct, suffix + "-lot", EventProductType.Reference, 18));
                    evt.SourceList.Add(BuildSource(EventSourceType.Location, sourceLocation));
                    evt.SourceList.Add(BuildSource(EventSourceType.Owner, ProductOwner));
                    evt.DestinationList.Add(BuildDestination(EventDestinationType.Location, destinationLocation));
                    evt.DestinationList.Add(BuildDestination(EventDestinationType.Owner, DestinationOwner));
                    doc.Events.Add(evt);
                    return doc;
                },
                evt =>
                {
                    var gdstEvent = AssertEventType<GDSTShippingEvent>(evt);
                    Assert.That(gdstEvent.UnloadingPort, Is.EqualTo("urn:gdst:test:location:loc:port.shipping"));
                    Assert.That(gdstEvent.SourceList.Any(s => s.ParsedType == EventSourceType.Location && s.Value == sourceLocation), Is.True);
                    Assert.That(gdstEvent.DestinationList.Any(d => d.ParsedType == EventDestinationType.Location && d.Value == destinationLocation), Is.True);
                });
        }

        private static ProfileRoundTripCase BuildReceivingProfileCase(
            string name,
            GDSTEventProfile expectedProfile,
            string[] certificateTypes,
            string sourceLocation,
            string destinationLocation,
            string suffix,
            string? humanWelfarePolicy,
            string? unloadingPort)
        {
            return new ProfileRoundTripCase(
                name,
                typeof(GDSTReceivingEvent),
                expectedProfile,
                certificateTypes,
                () =>
                {
                    EPCISDocument doc = BuildBaseDocument();
                    var evt = new GDSTReceivingEvent
                    {
                        HumanWelfarePolicy = humanWelfarePolicy,
                        UnloadingPort = unloadingPort
                    };
                    ApplyCommonFields(evt, suffix, destinationLocation);
                    evt.CertificationList = BuildCertificationList(certificateTypes);
                    evt.AddProduct(BuildProduct(GenericProduct, suffix + "-lot", EventProductType.Reference, 18));
                    evt.SourceList.Add(BuildSource(EventSourceType.Location, sourceLocation));
                    evt.SourceList.Add(BuildSource(EventSourceType.Owner, ProductOwner));
                    evt.DestinationList.Add(BuildDestination(EventDestinationType.Location, destinationLocation));
                    evt.DestinationList.Add(BuildDestination(EventDestinationType.Owner, DestinationOwner));
                    doc.Events.Add(evt);
                    return doc;
                },
                evt =>
                {
                    var gdstEvent = AssertEventType<GDSTReceivingEvent>(evt);
                    Assert.That(gdstEvent.HumanWelfarePolicy, Is.EqualTo(humanWelfarePolicy));
                    Assert.That(gdstEvent.UnloadingPort, Is.EqualTo(unloadingPort));
                    Assert.That(gdstEvent.SourceList.Any(s => s.ParsedType == EventSourceType.Location && s.Value == sourceLocation), Is.True);
                    Assert.That(gdstEvent.DestinationList.Any(d => d.ParsedType == EventDestinationType.Location && d.Value == destinationLocation), Is.True);
                });
        }

        private static ProfileRoundTripCase BuildTransformationProfileCase(
            string name,
            GDSTEventProfile expectedProfile,
            string[] certificateTypes,
            string bizLocation,
            string inputProduct,
            string outputProduct,
            string suffix,
            string? humanWelfarePolicy,
            string? proteinSource,
            string? aquacultureMethod)
        {
            return new ProfileRoundTripCase(
                name,
                typeof(GDSTTransformationEvent),
                expectedProfile,
                certificateTypes,
                () =>
                {
                    EPCISDocument doc = BuildBaseDocument();
                    var evt = new GDSTTransformationEvent
                    {
                        ProductOwner = new PGLN(ProductOwner),
                        HumanWelfarePolicy = humanWelfarePolicy,
                        ILMD = new GDSTILMD
                        {
                            ProteinSource = proteinSource,
                            AquacultureMethod = aquacultureMethod,
                            ProductionMethodForFishAndSeafoodCode = "AQUACULTURE",
                            LotNumber = suffix.ToUpperInvariant() + "-OUTPUT"
                        }
                    };
                    ApplyCommonFields(evt, suffix, bizLocation);
                    evt.CertificationList = BuildCertificationList(certificateTypes);
                    evt.AddProduct(BuildProduct(inputProduct, suffix + "-input", EventProductType.Input, 30));
                    evt.AddProduct(BuildProduct(outputProduct, suffix + "-output", EventProductType.Output, 24));
                    doc.Events.Add(evt);
                    return doc;
                },
                evt =>
                {
                    var gdstEvent = AssertEventType<GDSTTransformationEvent>(evt);
                    Assert.That(gdstEvent.ProductOwner?.ToString(), Is.EqualTo(ProductOwner));
                    Assert.That(gdstEvent.HumanWelfarePolicy, Is.EqualTo(humanWelfarePolicy));
                    Assert.That(gdstEvent.ILMD?.ProteinSource, Is.EqualTo(proteinSource));
                    Assert.That(gdstEvent.ILMD?.AquacultureMethod, Is.EqualTo(aquacultureMethod));
                    Assert.That(gdstEvent.Products.Count(p => p.Type == EventProductType.Input), Is.EqualTo(1));
                    Assert.That(gdstEvent.Products.Count(p => p.Type == EventProductType.Output), Is.EqualTo(1));
                });
        }

        private static EPCISDocument BuildBaseDocument()
        {
            var doc = new EPCISDocument
            {
                EPCISVersion = EPCISVersion.V2,
                CreationDate = new DateTimeOffset(2026, 5, 14, 12, 0, 0, TimeSpan.Zero)
            };

            AddTradeItem(doc, WildProduct, "WildCaught");
            AddTradeItem(doc, DevelopingProduct, "Developing");
            AddTradeItem(doc, FeedProduct, "Feed");
            AddTradeItem(doc, MatureProduct, "Mature");
            AddTradeItem(doc, ProcessedProduct, "Processed");
            AddTradeItem(doc, CommingledProduct, "Processed");
            AddTradeItem(doc, GenericProduct, "Processed");

            doc.MasterData.Add(BuildLocation(VesselOne, "Vessel"));
            doc.MasterData.Add(BuildLocation(VesselTwo, "Vessel"));
            doc.MasterData.Add(BuildLocation(LandFacilityOne, "Land Facility"));
            doc.MasterData.Add(BuildLocation(LandFacilityTwo, "Land Facility"));
            doc.MasterData.Add(BuildLocation(Hatchery, "Land Facility"));
            doc.MasterData.Add(BuildLocation(Processor, "Land Facility"));

            return doc;
        }

        private static void AddTradeItem(EPCISDocument doc, string productID, string classification)
        {
            doc.MasterData.Add(new GDSTTradeItem
            {
                GTIN = new GTIN(productID),
                ProductClassification =
                {
                    new GDSTClassification { Type = "GDST", Value = classification }
                }
            });
        }

        private static void ApplyCommonFields(IEvent evt, string suffix, string bizLocation)
        {
            evt.EventID = new Uri("urn:gdst:test:event:" + suffix);
            evt.EventTime = new DateTimeOffset(2026, 5, 14, 8, 30, 0, TimeSpan.FromHours(-6));
            evt.RecordTime = new DateTimeOffset(2026, 5, 14, 14, 45, 0, TimeSpan.Zero);
            evt.EventTimeZoneOffset = TimeSpan.FromHours(-6);
            evt.CertificationInfo = "certificate-info-" + suffix;
            if (evt is EventBase eventBase)
            {
                eventBase.InformationProvider = new PGLN(InformationProvider);
            }
            evt.ReadPoint = new EventReadPoint
            {
                ID = new Uri("urn:gdst:test:readpoint:" + suffix)
            };
            evt.Location = new EventLocation(new GLN(bizLocation));
        }

        private static GDSTILMD BuildWildCatchILMD()
        {
            return new GDSTILMD
            {
                ProductionMethodForFishAndSeafoodCode = "WILD_CAUGHT",
                LotNumber = "FISHING-LOT-1",
                VesselCatchInformationList = new VesselCatchInformationList
                {
                    Vessels =
                    {
                        new VesselCatchInformation
                        {
                            CatchArea = "27",
                            EconomicZone = "US",
                            GearType = "LL",
                            VesselFlagState = Countries.FromAbbreviation("US"),
                            VesselID = "VESSEL-ID-001",
                            VesselName = "GDST Test Vessel",
                            FIP = "Example Fishery Improvement Project",
                            GPSAvailability = true,
                            IMONumber = "IMO1234567",
                            RFMO = "ICCAT",
                            SatelliteTrackingAuthority = "US",
                            SubNationalPermitArea = "Gulf Test Area",
                            VesselPublicRegistry = "https://example.org/vessel-registry/1",
                            VesselTripDate = new DateTimeOffset(2026, 5, 1, 0, 0, 0, TimeSpan.Zero)
                        }
                    }
                }
            };
        }

        private static EventProduct BuildProduct(string productClass, string lot, EventProductType type, double quantity)
        {
            return new EventProduct(new EPC(productClass.Replace(":product:class:", ":product:lot:class:") + "." + lot))
            {
                Type = type,
                Quantity = new Measurement(quantity, "KGM")
            };
        }

        private static CertificationList BuildCertificationList(params string[] certificateTypes)
        {
            return new CertificationList
            {
                Certificates = certificateTypes.Select(BuildCertificate).ToList()
            };
        }

        private static Certificate BuildCertificate(string certificateType)
        {
            string suffix = certificateType.Split(':').Last();
            return new Certificate
            {
                CertificateType = certificateType,
                Agency = "Agency " + suffix,
                Standard = "Standard " + suffix,
                Value = "Value " + suffix,
                Identification = "ID-" + suffix,
                StartDate = new DateTimeOffset(2026, 1, 1, 0, 0, 0, TimeSpan.Zero),
                EndDate = new DateTimeOffset(2026, 12, 31, 0, 0, 0, TimeSpan.Zero)
            };
        }

        private static void AssertProfileCase(ProfileRoundTripCase testCase, EPCISDocument doc, IEvent evt)
        {
            Assert.That(evt, Is.TypeOf(testCase.ExpectedEventType));
            Assert.That(GDSTEventProfileResolver.Resolve(evt, doc), Is.EqualTo(testCase.ExpectedProfile));
            AssertCertificates(evt, testCase.ExpectedCertificateTypes);
        }

        private static void AssertRoundTrippedEvent(IEvent expected, IEvent actual)
        {
            Assert.That(actual.EventID?.ToString(), Is.EqualTo(expected.EventID?.ToString()));
            Assert.That(actual.EventTime, Is.EqualTo(expected.EventTime));
            Assert.That(actual.RecordTime, Is.EqualTo(expected.RecordTime));
            Assert.That(actual.EventTimeZoneOffset, Is.EqualTo(expected.EventTimeZoneOffset));
            Assert.That(actual.CertificationInfo, Is.EqualTo(expected.CertificationInfo));
            Assert.That((actual as EventBase)?.InformationProvider?.ToString(), Is.EqualTo((expected as EventBase)?.InformationProvider?.ToString()));
            Assert.That(actual.EventType, Is.EqualTo(expected.EventType));
            Assert.That(actual.Action, Is.EqualTo(expected.Action));
            Assert.That(NormalizeCBVUri(actual.BusinessStep), Is.EqualTo(NormalizeCBVUri(expected.BusinessStep)));
            Assert.That(NormalizeCBVUri(actual.Disposition), Is.EqualTo(NormalizeCBVUri(expected.Disposition)));
            Assert.That(actual.ReadPoint?.ID?.ToString(), Is.EqualTo(expected.ReadPoint?.ID?.ToString()));
            Assert.That(actual.Location?.GLN?.ToString(), Is.EqualTo(expected.Location?.GLN?.ToString()));
            Assert.That(actual.Products.Select(ProductKey), Is.EquivalentTo(expected.Products.Select(ProductKey)));
        }

        private static string ProductKey(EventProduct product)
        {
            string quantity = product.Quantity == null ? string.Empty : $"{product.Quantity.Value}:{product.Quantity.UoM?.UNCode}";
            return $"{product.Type}|{product.EPC}|{quantity}";
        }

        private static string? NormalizeCBVUri(Uri? uri)
        {
            string? value = uri?.ToString();
            if (value == null)
            {
                return null;
            }

            return value
                .Replace("https://ref.gs1.org/cbv/BizStep-", "urn:epcglobal:cbv:bizstep:")
                .Replace("https://ref.gs1.org/cbv/Disp-", string.Empty)
                .ToLowerInvariant();
        }

        private static void AssertMasterDataRoundTripped(EPCISDocument expected, EPCISDocument actual)
        {
            foreach (GDSTTradeItem expectedTradeItem in expected.GetMasterData<GDSTTradeItem>())
            {
                GDSTTradeItem actualTradeItem = actual.GetMasterData<GDSTTradeItem>(expectedTradeItem.ID);
                Assert.That(actualTradeItem, Is.Not.Null, "Expected trade item master data to round trip: " + expectedTradeItem.ID);
                Assert.That(
                    actualTradeItem.ProductClassification.Select(c => c.Value),
                    Is.EquivalentTo(expectedTradeItem.ProductClassification.Select(c => c.Value)));
            }

            foreach (GDSTLocation expectedLocation in expected.GetMasterData<GDSTLocation>())
            {
                GDSTLocation actualLocation = actual.GetMasterData<GDSTLocation>(expectedLocation.ID);
                Assert.That(actualLocation, Is.Not.Null, "Expected location master data to round trip: " + expectedLocation.ID);
                Assert.That(
                    actualLocation.LocationClassification.Select(c => c.Value),
                    Is.EquivalentTo(expectedLocation.LocationClassification.Select(c => c.Value)));
            }
        }

        private static void AssertCertificates(IEvent evt, string[] expectedCertificateTypes)
        {
            List<Certificate> actualCertificates = evt.CertificationList?.Certificates ?? new List<Certificate>();
            Assert.That(actualCertificates, Has.Count.EqualTo(expectedCertificateTypes.Length));

            foreach (string expectedCertificateType in expectedCertificateTypes)
            {
                Certificate expected = BuildCertificate(expectedCertificateType);
                Certificate? actual = actualCertificates.SingleOrDefault(c => c.CertificateType == expectedCertificateType);
                Assert.That(actual, Is.Not.Null, "Expected certificate type to round trip: " + expectedCertificateType);
                Assert.That(actual!.Agency, Is.EqualTo(expected.Agency));
                Assert.That(actual.Standard, Is.EqualTo(expected.Standard));
                Assert.That(actual.Value, Is.EqualTo(expected.Value));
                Assert.That(actual.Identification, Is.EqualTo(expected.Identification));
                Assert.That(actual.StartDate, Is.EqualTo(expected.StartDate));
                Assert.That(actual.EndDate, Is.EqualTo(expected.EndDate));
            }
        }

        private static T AssertEventType<T>(IEvent evt) where T : IEvent
        {
            Assert.That(evt, Is.TypeOf<T>());
            return (T)evt;
        }

        private static void AssertWildCatchILMD(GDSTILMD? ilmd)
        {
            Assert.That(ilmd, Is.Not.Null);
            Assert.That(ilmd!.ProductionMethodForFishAndSeafoodCode, Is.EqualTo("WILD_CAUGHT"));
            Assert.That(ilmd.LotNumber, Is.EqualTo("FISHING-LOT-1"));
            Assert.That(ilmd.VesselCatchInformationList, Is.Not.Null);
            VesselCatchInformation vessel = ilmd.VesselCatchInformationList!.Vessels.Single();
            Assert.That(vessel.CatchArea, Is.EqualTo("27"));
            Assert.That(vessel.EconomicZone, Is.EqualTo("US"));
            Assert.That(vessel.GearType, Is.EqualTo("LL"));
            Assert.That(vessel.VesselFlagState?.Abbreviation, Is.EqualTo("US"));
            Assert.That(vessel.VesselID, Is.EqualTo("VESSEL-ID-001"));
            Assert.That(vessel.VesselName, Is.EqualTo("GDST Test Vessel"));
            Assert.That(vessel.FIP, Is.EqualTo("Example Fishery Improvement Project"));
            Assert.That(vessel.GPSAvailability, Is.True);
            Assert.That(vessel.IMONumber, Is.EqualTo("IMO1234567"));
            Assert.That(vessel.RFMO, Is.EqualTo("ICCAT"));
            Assert.That(vessel.SatelliteTrackingAuthority, Is.EqualTo("US"));
            Assert.That(vessel.SubNationalPermitArea, Is.EqualTo("Gulf Test Area"));
            Assert.That(vessel.VesselPublicRegistry, Is.EqualTo("https://example.org/vessel-registry/1"));
            Assert.That(vessel.VesselTripDate, Is.EqualTo(new DateTimeOffset(2026, 5, 1, 0, 0, 0, TimeSpan.Zero)));
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

        private static EventSource BuildSource(EventSourceType type, string value)
        {
            return new EventSource
            {
                ParsedType = type,
                Value = value
            };
        }

        private static EventDestination BuildDestination(EventDestinationType type, string value)
        {
            return new EventDestination
            {
                ParsedType = type,
                Value = value
            };
        }

        private static void AssertCombination(IEvent evt, EventType eventType, EventAction? action, string businessStep)
        {
            Assert.That(evt.EventType, Is.EqualTo(eventType));
            Assert.That(evt.Action, Is.EqualTo(action));
            Assert.That(evt.BusinessStep.ToString(), Is.EqualTo(businessStep));
        }

        public sealed class ProfileRoundTripCase
        {
            public ProfileRoundTripCase(
                string name,
                Type expectedEventType,
                GDSTEventProfile expectedProfile,
                string[] expectedCertificateTypes,
                Func<EPCISDocument> buildDocument,
                Action<IEvent> assertProfileProperties)
            {
                Name = name;
                ExpectedEventType = expectedEventType;
                ExpectedProfile = expectedProfile;
                ExpectedCertificateTypes = expectedCertificateTypes;
                BuildDocument = buildDocument;
                AssertProfileProperties = assertProfileProperties;
            }

            public string Name { get; }
            public Type ExpectedEventType { get; }
            public GDSTEventProfile ExpectedProfile { get; }
            public string[] ExpectedCertificateTypes { get; }
            public Func<EPCISDocument> BuildDocument { get; }
            public Action<IEvent> AssertProfileProperties { get; }

            public override string ToString()
            {
                return Name;
            }
        }
    }
}
