using Newtonsoft.Json.Linq;
using OpenTraceability.TestServer.Core.Modules;

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
    }
}
