using Newtonsoft.Json.Linq;
using OpenTraceability.GDST;
using OpenTraceability.GDST.Modules;

namespace OpenTraceability.Tests.TestServer
{
    public class ModuleMinifierTests
    {
        private const string Sample = @"{
            ""eventID"": ""urn:uuid:1"",
            ""bizStep"": ""commissioning"",
            ""gdst:broodstockSource"": ""hatchery-1"",
            ""gdst:aquacultureMethod"": ""pond"",
            ""cbvmda:vesselCatchInformationList"": { ""cbvmda:vesselName"": ""Boaty"" },
            ""cbvmda:unloadingPort"": ""Port of San Diego"",
            ""emptyString"": """",
            ""nullValue"": null,
            ""emptyArray"": [],
            ""epcList"": [ ""urn:epc:id:sgtin:1"" ]
        }";

        [Test]
        public void CoreOnly_StripsAllNonCoreModuleKeys()
        {
            var allowed = ModuleSet.Expand(new[] { GdstModule.Core });
            var result = JObject.Parse(ModuleMinifier.Minify(Sample, allowed));

            Assert.That(result.ContainsKey("eventID"), Is.True);
            Assert.That(result.ContainsKey("bizStep"), Is.True);
            Assert.That(result.ContainsKey("epcList"), Is.True);

            Assert.That(result.ContainsKey("gdst:broodstockSource"), Is.False);
            Assert.That(result.ContainsKey("gdst:aquacultureMethod"), Is.False);
            Assert.That(result.ContainsKey("cbvmda:vesselCatchInformationList"), Is.False);
            Assert.That(result.ContainsKey("cbvmda:unloadingPort"), Is.False);
        }

        [Test]
        public void CoreOnly_PrunesNullsAndEmpties()
        {
            var allowed = ModuleSet.Expand(new[] { GdstModule.Core });
            var result = JObject.Parse(ModuleMinifier.Minify(Sample, allowed));

            Assert.That(result.ContainsKey("emptyString"), Is.False);
            Assert.That(result.ContainsKey("nullValue"), Is.False);
            Assert.That(result.ContainsKey("emptyArray"), Is.False);
        }

        [Test]
        public void Wildcaught_KeepsVesselAndSeafood_RemovesAquaculture()
        {
            // Wildcaught implies Seafood, but not Aquaculture.
            var allowed = ModuleSet.Expand(new[] { GdstModule.Wildcaught });
            var result = JObject.Parse(ModuleMinifier.Minify(Sample, allowed));

            Assert.That(result.ContainsKey("cbvmda:vesselCatchInformationList"), Is.True, "wildcaught key kept");
            Assert.That(result.ContainsKey("cbvmda:unloadingPort"), Is.True, "seafood key kept (implied)");
            Assert.That(result.ContainsKey("gdst:broodstockSource"), Is.False, "aquaculture key removed");
            Assert.That(result.ContainsKey("gdst:aquacultureMethod"), Is.False, "aquaculture key removed");
        }

        [Test]
        public void AquacultureAndWildcaught_KeepsEverything()
        {
            var allowed = ModuleSet.Expand(new[] { GdstModule.Wildcaught, GdstModule.Aquaculture });
            var result = JObject.Parse(ModuleMinifier.Minify(Sample, allowed));

            Assert.That(result.ContainsKey("cbvmda:vesselCatchInformationList"), Is.True);
            Assert.That(result.ContainsKey("gdst:broodstockSource"), Is.True);
            Assert.That(result.ContainsKey("gdst:aquacultureMethod"), Is.True);
            Assert.That(result.ContainsKey("cbvmda:unloadingPort"), Is.True);
        }

        [Test]
        public void ModuleSet_Expand_AddsCoreAndImpliedSeafood()
        {
            var aqua = ModuleSet.Expand(new[] { GdstModule.Aquaculture });
            Assert.That(aqua, Does.Contain(GdstModule.Core));
            Assert.That(aqua, Does.Contain(GdstModule.Seafood));
            Assert.That(aqua, Does.Contain(GdstModule.Aquaculture));
            Assert.That(aqua, Does.Not.Contain(GdstModule.Wildcaught));
        }

        private const string SourceDestSample = @"{
            ""eventID"": ""urn:uuid:1"",
            ""sourceList"": [
                { ""type"": ""location"", ""source"": ""urn:gdst:loc:vessel1"" },
                { ""type"": ""owning_party"", ""source"": ""urn:gdst:party:fisherman"" }
            ],
            ""destinationList"": [
                { ""type"": ""location"", ""destination"": ""urn:gdst:loc:vessel2"" },
                { ""type"": ""owning_party"", ""destination"": ""urn:gdst:party:processor"" }
            ]
        }";

        [Test]
        public void CoreOnly_StripsPartyEntries_KeepsLocationAndList()
        {
            var allowed = ModuleSet.Expand(new[] { GdstModule.Core });
            var result = JObject.Parse(ModuleMinifier.Minify(SourceDestSample, allowed));

            var sourceList = (JArray)result["sourceList"]!;
            Assert.That(sourceList, Has.Count.EqualTo(1), "owning_party source entry removed");
            Assert.That(sourceList[0]["type"]!.Value<string>(), Is.EqualTo("location"));

            var destinationList = (JArray)result["destinationList"]!;
            Assert.That(destinationList, Has.Count.EqualTo(1), "owning_party destination entry removed");
            Assert.That(destinationList[0]["type"]!.Value<string>(), Is.EqualTo("location"));
            Assert.That(destinationList[0].Value<JObject>()!.ContainsKey("destination"), Is.True);
        }

        [Test]
        public void Seafood_KeepsPartyEntries()
        {
            var allowed = ModuleSet.Expand(new[] { GdstModule.Wildcaught }); // implies Seafood
            var result = JObject.Parse(ModuleMinifier.Minify(SourceDestSample, allowed));

            Assert.That((JArray)result["sourceList"]!, Has.Count.EqualTo(2), "party entries kept");
            Assert.That((JArray)result["destinationList"]!, Has.Count.EqualTo(2), "party entries kept");
        }

        [Test]
        public void CoreOnly_PartyOnlyList_CollapsesEntirely()
        {
            const string partyOnly = @"{
                ""eventID"": ""urn:uuid:1"",
                ""sourceList"": [
                    { ""type"": ""owning_party"", ""source"": ""urn:gdst:party:fisherman"" }
                ]
            }";

            var allowed = ModuleSet.Expand(new[] { GdstModule.Core });
            var result = JObject.Parse(ModuleMinifier.Minify(partyOnly, allowed));

            Assert.That(result.ContainsKey("sourceList"), Is.False, "empty list pruned after stripping its only entry");
        }

        // A location and a trade item in the GS1 Web Vocab master-data shape, carrying one
        // classification value per module plus an unknown value that no module owns.
        private const string ClassificationSample = @"{
            ""location"": {
                ""globalLocationNumber"": ""urn:gdst:loc:1"",
                ""gdst:locationClassification"": [
                    { ""type"": ""GDST"", ""value"": ""Land Facility"" },
                    { ""type"": ""GDST"", ""value"": ""processor"" },
                    { ""type"": ""GDST"", ""value"": ""vessel"" },
                ]
            },
            ""tradeitem"": {
                ""gtin"": ""urn:gdst:product:1"",
                ""gdst:productClassification"": [
                    { ""type"": ""GDST"", ""value"": ""seafood"" },
                    { ""type"": ""GDST"", ""value"": ""processed"" },
                    { ""type"": ""GDST"", ""value"": ""WildCaught"" },
                    { ""type"": ""GDST"", ""value"": ""farmed"" },
                    { ""type"": ""GDST"", ""value"": ""feed"" },
                    { ""type"": ""GDST"", ""value"": ""developing"" },
                    { ""type"": ""GDST"", ""value"": ""mature"" },
                    { ""type"": ""GDST"", ""value"": ""live"" },
                ]
            }
        }";

        private static List<string?> ClassificationValues(JObject root, string element, string key)
        {
            var list = root[element]?[key] as JArray ?? new JArray();
            return list.Select(t => t["value"]?.Value<string>()).ToList();
        }

        [Test]
        public void CoreOnly_Classifications_KeepsOnlyLandFacility()
        {
            var allowed = ModuleSet.Expand(new[] { GdstModule.Core });
            var result = JObject.Parse(ModuleMinifier.Minify(ClassificationSample, allowed));

            Assert.That(ClassificationValues(result, "location", "gdst:locationClassification"), Is.EqualTo(new[] { "Land Facility" }), "core keeps only the land facility location classification");
            Assert.That(((JObject)result["tradeitem"]!).ContainsKey("gdst:productClassification"), Is.False, "no product classification is core-owned, so the emptied list is pruned");
        }

        [Test]
        public void Seafood_Classifications_KeepsSeafoodValues_StripsOthers()
        {
            var allowed = ModuleSet.Expand(new[] { GdstModule.Seafood });
            var result = JObject.Parse(ModuleMinifier.Minify(ClassificationSample, allowed));

            Assert.That(ClassificationValues(result, "location", "gdst:locationClassification"), Is.EqualTo(new[] { "Land Facility", "processor" }), "seafood keeps land facility (core) and processor");
            Assert.That(ClassificationValues(result, "tradeitem", "gdst:productClassification"), Is.EqualTo(new[] { "seafood", "processed" }), "seafood keeps only its product values; unknown 'organic' is stripped");
        }

        [Test]
        public void Wildcaught_Classifications_KeepsVesselAndWildcaught()
        {
            // Wildcaught implies Seafood, so the seafood values are kept too.
            var allowed = ModuleSet.Expand(new[] { GdstModule.Wildcaught });
            var result = JObject.Parse(ModuleMinifier.Minify(ClassificationSample, allowed));

            Assert.That(ClassificationValues(result, "location", "gdst:locationClassification"), Is.EqualTo(new[] { "Land Facility", "processor", "vessel" }));
            Assert.That(ClassificationValues(result, "tradeitem", "gdst:productClassification"), Is.EqualTo(new[] { "seafood", "processed", "WildCaught" }));
        }

        [Test]
        public void Aquaculture_Classifications_KeepsAquacultureProductValues()
        {
            // Aquaculture implies Seafood but not Wildcaught, so vessel and WildCaught are stripped.
            var allowed = ModuleSet.Expand(new[] { GdstModule.Aquaculture });
            var result = JObject.Parse(ModuleMinifier.Minify(ClassificationSample, allowed));

            Assert.That(ClassificationValues(result, "location", "gdst:locationClassification"), Is.EqualTo(new[] { "Land Facility", "processor" }));
            Assert.That(ClassificationValues(result, "tradeitem", "gdst:productClassification"), Is.EqualTo(new[] { "seafood", "processed", "farmed", "feed", "developing", "mature", "live" }));
        }
    }
}
