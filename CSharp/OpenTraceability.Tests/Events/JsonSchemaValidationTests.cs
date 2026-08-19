using OpenTraceability.Utility;

namespace OpenTraceability.Tests.Events
{
    /// <summary>
    /// MSC-180. Tests for the structured validation API.
    ///
    /// Unlike the characterization tests, these do assert correctness.
    /// </summary>
    [TestFixture]
    [Category("UnitTest")]
    public class JsonSchemaValidationTests
    {
        private const string BrokenEventFile = "msc180_event_broken.jsonld";
        private const string ValidEventFile = "msc180_event_valid.jsonld";
        private const string GdstSchemaKey = "GDST";

        [Test]
        public async Task Validate_BrokenEvent_ReportsTheOffendingFieldPaths()
        {
            string json = OpenTraceabilityTests.ReadTestData(BrokenEventFile);

            JsonSchemaValidationResult result = await JsonSchemaChecker.ValidateAsync(json, GdstSchemaKey);

            Assert.That(result.IsValid, Is.False);

            List<string> locations = result.Errors.Select(e => e.InstanceLocation).ToList();

            Assert.Multiple(() =>
            {
                Assert.That(locations, Does.Contain("/bizStep"));
                Assert.That(locations, Does.Contain("/disposition"));
            });
        }

        [Test]
        public async Task Validate_BrokenEvent_ReportsTheOffendingValue()
        {
            string json = OpenTraceabilityTests.ReadTestData(BrokenEventFile);

            JsonSchemaValidationResult result = await JsonSchemaChecker.ValidateAsync(json, GdstSchemaKey);

            JsonSchemaValidationError bizStep = result.Errors.Single(e => e.InstanceLocation == "/bizStep");

            Assert.That(bizStep.Value, Is.EqualTo("urn:epcglobal:cbv:bizstep:commissioning"));
        }

        /// <summary>
        /// bizStep is an anyOf over a URI pattern and a CBV shorthand enum. A full CBV URI fails both,
        /// so both reasons have to be grouped under the one field rather than reported separately.
        /// </summary>
        [Test]
        public async Task Validate_BrokenEvent_GroupsEveryReasonUnderItsField()
        {
            string json = OpenTraceabilityTests.ReadTestData(BrokenEventFile);

            JsonSchemaValidationResult result = await JsonSchemaChecker.ValidateAsync(json, GdstSchemaKey);

            JsonSchemaValidationError bizStep = result.Errors.Single(e => e.InstanceLocation == "/bizStep");
            List<string> keywords = bizStep.Reasons.Select(r => r.Keyword).ToList();

            Assert.Multiple(() =>
            {
                Assert.That(keywords, Does.Contain("pattern"));
                Assert.That(keywords, Does.Contain("enum"));
            });
        }

        [Test]
        public async Task Validate_ValidEvent_ReportsNothing()
        {
            string json = OpenTraceabilityTests.ReadTestData(ValidEventFile);

            JsonSchemaValidationResult result = await JsonSchemaChecker.ValidateAsync(json, GdstSchemaKey);

            Assert.Multiple(() =>
            {
                Assert.That(result.IsValid, Is.True);
                Assert.That(result.Errors, Is.Empty);
            });
        }

        /// <summary>
        /// This commit is additive. The existing string API has to behave exactly as before.
        /// </summary>
        [Test]
        public async Task IsValidAsync_BrokenEvent_IsUnchanged()
        {
            string json = OpenTraceabilityTests.ReadTestData(BrokenEventFile);

            List<string> errors = await JsonSchemaChecker.IsValidAsync(json, GdstSchemaKey);

            Assert.That(errors, Has.Count.EqualTo(5));
        }
    }
}

