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
        private const string DocumentRoot = "";

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

        /// <summary>
        /// The reported event has exactly three defects: bizStep and disposition use full CBV URIs
        /// where the schema wants the shorthand, and @context is missing. Everything else the
        /// validator emits comes from branches that did not affect the outcome.
        /// </summary>
        [Test]
        public async Task Validate_BrokenEvent_ReportsOnlyTheRealFailures()
        {
            string json = OpenTraceabilityTests.ReadTestData(BrokenEventFile);

            JsonSchemaValidationResult result = await JsonSchemaChecker.ValidateAsync(json, GdstSchemaKey);

            List<string> locations = result.Errors.Select(e => e.InstanceLocation).ToList();

            Assert.That(locations, Is.EquivalentTo(new[] { "/bizStep", "/disposition", DocumentRoot }));
        }

        /// <summary>
        /// These fields are correct in the reported event. They were previously reported because the
        /// losing side of an anyOf, and every "if" the schema used to select the event type, were
        /// harvested alongside the real failures.
        /// </summary>
        [TestCase("/type")]
        [TestCase("/action")]
        [TestCase("/eventTime")]
        [TestCase("/eventTimeZoneOffset")]
        [TestCase("/epcList")]
        [TestCase("/quantityList")]
        public async Task Validate_BrokenEvent_DoesNotReportValidFields(string location)
        {
            string json = OpenTraceabilityTests.ReadTestData(BrokenEventFile);

            JsonSchemaValidationResult result = await JsonSchemaChecker.ValidateAsync(json, GdstSchemaKey);

            List<string> locations = result.Errors.Select(e => e.InstanceLocation).ToList();

            Assert.That(locations, Does.Not.Contain(location));
        }

        /// <summary>
        /// sensorElementList and readPoint are not required for this event type. They came from an
        /// anyOf branch that did not apply, and the reporter spent time chasing them.
        /// </summary>
        [Test]
        public async Task Validate_BrokenEvent_DoesNotReportFieldsFromBranchesThatDoNotApply()
        {
            string json = OpenTraceabilityTests.ReadTestData(BrokenEventFile);

            JsonSchemaValidationResult result = await JsonSchemaChecker.ValidateAsync(json, GdstSchemaKey);

            string everything = string.Join("\n", result.Errors.Select(e => e.ToString()));

            Assert.Multiple(() =>
            {
                Assert.That(everything, Does.Not.Contain("sensorElementList"));
                Assert.That(everything, Does.Not.Contain("readPoint"));
                Assert.That(everything, Does.Contain("@context"));
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
        /// The change stays additive. The existing string API has to behave exactly as before until
        /// it is rewritten in its own commit.
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

