using System.Text;
using System.Text.Json;
using Json.Schema;
using OpenTraceability.Utility;

namespace OpenTraceability.Tests.Events
{
    /// <summary>
    /// MSC-180 characterization tests.
    ///
    /// These tests do not assert that the current behaviour is correct. They record it, so the same
    /// tests run after the change produce a diff that can be attached to the card and the PR.
    ///
    /// Capture the output files once before implementing, keep that copy, then compare against a
    /// fresh run afterwards.
    /// </summary>
    [TestFixture]
    [Category("UnitTest")]
    public class SchemaErrorRenderingBaselineTests
    {
        private const string BrokenEventFile = "msc180_event_broken.jsonld";
        private const string ValidEventFile = "msc180_event_valid.jsonld";
        private const string GdstSchemaKey = "GDST";
        private const string GdstSchemaResource = "OpenTraceability.Utility.Data.gdst_json_schema.json";

        /// <summary>
        /// Captures exactly what a caller of the public API sees: the reported case, rendered the way
        /// the reporter received it.
        /// </summary>
        [Test]
        public async Task Baseline_PublicApiOutput()
        {
            string json = OpenTraceabilityTests.ReadTestData(BrokenEventFile);

            List<string> errors = await JsonSchemaChecker.IsValidAsync(json, GdstSchemaKey);

            var report = new StringBuilder();
            report.AppendLine($"errors returned   : {errors.Count}");
            report.AppendLine($"distinct messages : {errors.Distinct().Count()}");
            report.AppendLine();

            foreach (string error in errors)
            {
                report.AppendLine(error);
            }

            Save("msc180_public_api.txt", report.ToString());

            // The fixture has to keep failing validation, otherwise the capture means nothing.
            Assert.That(errors, Is.Not.Empty);
        }

        /// <summary>
        /// Control case. The same event with the three genuine defects fixed - CBV shorthand for
        /// bizStep and disposition, and the missing @context - must validate cleanly.
        ///
        /// This is what proves the remaining nodes reported against the broken document are noise:
        /// correcting three fields removes all of them.
        /// </summary>
        [Test]
        public async Task Control_ValidEvent_HasNoErrors()
        {
            string json = OpenTraceabilityTests.ReadTestData(ValidEventFile);

            List<string> errors = await JsonSchemaChecker.IsValidAsync(json, GdstSchemaKey);

            Save("msc180_control_valid.txt", string.Join(Environment.NewLine, errors));

            Assert.That(errors, Is.Empty);
        }

        private static void Save(string fileName, string content)
        {
            string path = Path.Combine(TestContext.CurrentContext.WorkDirectory, fileName);

            File.WriteAllText(path, content);
            TestContext.AddTestAttachment(path);
            TestContext.Out.WriteLine(content);
        }
    }
}