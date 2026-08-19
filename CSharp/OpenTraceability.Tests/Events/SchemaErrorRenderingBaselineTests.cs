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
        /// Dumps what JsonSchema.Net knows about the same document and labels every error node with
        /// the reason it is reported, so the noise can be counted rather than estimated.
        ///
        /// Labels:
        ///   if-dispatch     - the failure sits under an "if" keyword. A failing "if" is how a schema
        ///                     selects a branch, not a defect in the document.
        ///   ancestor-passed - an ancestor evaluation succeeded, so this branch did not affect the
        ///                     outcome. Typical of a losing "anyOf" branch.
        ///   unresolved      - neither rule applies. These are the real failures.
        /// </summary>
        [Test]
        public void Probe_ClassifyErrorNodes()
        {
            EvaluationResults results = Evaluate(BrokenEventFile);

            var counts = new Dictionary<string, int>
            {
                ["if-dispatch"] = 0,
                ["ancestor-passed"] = 0,
                ["unresolved"] = 0
            };

            var tree = new StringBuilder();

            void Walk(EvaluationResults node, bool anyAncestorValid, int depth)
            {
                bool hasErrors = node.Errors != null && node.Errors.Count > 0;

                if (hasErrors)
                {
                    string label = Classify(node, anyAncestorValid);
                    counts[label]++;

                    string indent = new string(' ', depth * 2);
                    tree.AppendLine($"{indent}[{label}] instance={node.InstanceLocation} evaluation={node.EvaluationPath}");

                    foreach (var error in node.Errors!)
                    {
                        tree.AppendLine($"{indent}    {error.Key} :: {error.Value}");
                    }
                }

                if (node.Details == null)
                {
                    return;
                }

                foreach (EvaluationResults child in node.Details)
                {
                    Walk(child, anyAncestorValid || node.IsValid, depth + 1);
                }
            }

            Walk(results, anyAncestorValid: false, depth: 0);

            int total = counts.Values.Sum();
            int noise = counts["if-dispatch"] + counts["ancestor-passed"];

            var report = new StringBuilder();
            report.AppendLine($"nodes carrying errors : {total}");
            report.AppendLine($"  if-dispatch         : {counts["if-dispatch"]}");
            report.AppendLine($"  ancestor-passed     : {counts["ancestor-passed"]}");
            report.AppendLine($"  unresolved          : {counts["unresolved"]}");
            report.AppendLine($"noise share           : {noise} of {total}");
            report.AppendLine();
            report.Append(tree);

            Save("msc180_classified.txt", report.ToString());

            Assert.That(results.IsValid, Is.False);
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

        private static string Classify(EvaluationResults node, bool anyAncestorValid)
        {
            if (node.EvaluationPath.ToString().Split('/').Contains("if"))
            {
                return "if-dispatch";
            }

            return anyAncestorValid ? "ancestor-passed" : "unresolved";
        }

        private static EvaluationResults Evaluate(string fixtureFile)
        {
            string json = OpenTraceabilityTests.ReadTestData(fixtureFile);

            string schemaText = new EmbeddedResourceLoader()
                .ReadString("OpenTraceability", GdstSchemaResource);

            JsonSchema schema = JsonSchema.FromText(
                schemaText,
                new BuildOptions { SchemaRegistry = new SchemaRegistry() });

            using JsonDocument document = JsonDocument.Parse(json);

            return schema.Evaluate(
                document.RootElement,
                new EvaluationOptions { OutputFormat = OutputFormat.Hierarchical });
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