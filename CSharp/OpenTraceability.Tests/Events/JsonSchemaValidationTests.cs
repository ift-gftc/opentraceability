using OpenTraceability.Utility;

namespace OpenTraceability.Tests.Events
{
    /// <summary>
    /// Tests for the structured validation API.
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

        /// <summary>The reported event has three genuinely wrong fields.</summary>
        private const int RealFailureCount = 3;

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
                Assert.That(result.TotalErrorCount, Is.Zero);
                Assert.That(result.OmittedErrorCount, Is.Zero);
            });
        }

        /// <summary>
        /// A realistic document must never be truncated by the default cap.
        /// </summary>
        [Test]
        public async Task Validate_BrokenEvent_IsNotTruncatedByTheDefaultCap()
        {
            string json = OpenTraceabilityTests.ReadTestData(BrokenEventFile);

            JsonSchemaValidationResult result = await JsonSchemaChecker.ValidateAsync(json, GdstSchemaKey);

            Assert.Multiple(() =>
            {
                Assert.That(result.Errors, Has.Count.EqualTo(RealFailureCount));
                Assert.That(result.TotalErrorCount, Is.EqualTo(RealFailureCount));
                Assert.That(result.OmittedErrorCount, Is.Zero);
            });
        }

        /// <summary>
        /// When the cap bites, the total has to stay truthful so a caller can report how many errors
        /// were left out rather than silently hiding them.
        /// </summary>
        [Test]
        public async Task Validate_BrokenEvent_KeepsTheTrueTotalWhenCapped()
        {
            string json = OpenTraceabilityTests.ReadTestData(BrokenEventFile);

            JsonSchemaValidationResult result = await JsonSchemaChecker.ValidateAsync(json, GdstSchemaKey, maxErrors: 1);

            Assert.Multiple(() =>
            {
                Assert.That(result.Errors, Has.Count.EqualTo(1));
                Assert.That(result.TotalErrorCount, Is.EqualTo(RealFailureCount));
                Assert.That(result.OmittedErrorCount, Is.EqualTo(RealFailureCount - 1));
                Assert.That(result.IsValid, Is.False);
            });
        }

        [TestCase(0)]
        [TestCase(-1)]
        public void Validate_RejectsANonPositiveCap(int maxErrors)
        {
            string json = OpenTraceabilityTests.ReadTestData(BrokenEventFile);

            Assert.ThrowsAsync<ArgumentOutOfRangeException>(
                async () => await JsonSchemaChecker.ValidateAsync(json, GdstSchemaKey, maxErrors));
        }

        /// <summary>
        /// The reported case, rendered. One entry per offending field instead of five keyword
        /// fragments, two of which named fields that were not wrong.
        /// </summary>
        [Test]
        public async Task IsValidAsync_BrokenEvent_ReportsOneEntryPerOffendingField()
        {
            string json = OpenTraceabilityTests.ReadTestData(BrokenEventFile);

            List<string> errors = await JsonSchemaChecker.IsValidAsync(json, GdstSchemaKey);

            Assert.That(errors, Has.Count.EqualTo(RealFailureCount));
        }

        [Test]
        public async Task IsValidAsync_BrokenEvent_NamesTheFieldAndTheValue()
        {
            string json = OpenTraceabilityTests.ReadTestData(BrokenEventFile);

            List<string> errors = await JsonSchemaChecker.IsValidAsync(json, GdstSchemaKey);
            string rendered = string.Join("\n", errors);

            Assert.Multiple(() =>
            {
                Assert.That(rendered, Does.Contain("/bizStep"));
                Assert.That(rendered, Does.Contain("urn:epcglobal:cbv:bizstep:commissioning"));
                Assert.That(rendered, Does.Contain("/disposition"));
                Assert.That(rendered, Does.Contain("urn:epcglobal:cbv:disp:active"));
            });
        }

        /// <summary>
        /// bizStep and disposition produce the same sentence from the same keyword. The old
        /// implementation ran Distinct over the whole document, so the two collapsed into one line
        /// and correcting either field appeared to change nothing.
        /// </summary>
        [Test]
        public async Task IsValidAsync_BrokenEvent_DoesNotMergeTwoFieldsIntoOneLine()
        {
            string json = OpenTraceabilityTests.ReadTestData(BrokenEventFile);

            List<string> errors = await JsonSchemaChecker.IsValidAsync(json, GdstSchemaKey);

            Assert.Multiple(() =>
            {
                Assert.That(errors.Count(e => e.Contains("/bizStep")), Is.EqualTo(1));
                Assert.That(errors.Count(e => e.Contains("/disposition")), Is.EqualTo(1));
            });
        }

        [Test]
        public async Task IsValidAsync_ValidEvent_ReportsNothing()
        {
            string json = OpenTraceabilityTests.ReadTestData(ValidEventFile);

            List<string> errors = await JsonSchemaChecker.IsValidAsync(json, GdstSchemaKey);

            Assert.That(errors, Is.Empty);
        }
    }
}