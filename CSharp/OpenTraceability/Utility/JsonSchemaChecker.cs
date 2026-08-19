using Json.Schema;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Text.Json;
using System.Threading;
using System.Threading.Tasks;

namespace OpenTraceability.Utility
{
    public static class JsonSchemaChecker
    {
        private const string EpcisSchemaUrl = "https://ref.gs1.org/standards/epcis/epcis-json-schema.json";
        private const string DigitalLinkSchemaKey = "DigitalLink";
        private const string LinksetSchemaKey = "Linkset";
        private const string GdstSchemaKey = "GDST";
        private const string EpcisBaseSchemaKey = "EPCIS_BASE";

        private static readonly HttpClient _httpClient = new HttpClient();

        private static readonly IReadOnlyDictionary<string, JsonSchema> _builtInSchemas;

        private static readonly ConcurrentDictionary<string, Lazy<Task<JsonSchema>>> _remoteSchemaCache =
            new ConcurrentDictionary<string, Lazy<Task<JsonSchema>>>(StringComparer.Ordinal);

        static JsonSchemaChecker()
        {
            var loader = new EmbeddedResourceLoader();

            _builtInSchemas = new Dictionary<string, JsonSchema>(StringComparer.Ordinal)
            {
                [EpcisSchemaUrl] = BuildSchema(
                    loader.ReadString(
                        "OpenTraceability",
                        "OpenTraceability.Utility.Data.EPCISJsonSchema.jsonld")),

                [DigitalLinkSchemaKey] = BuildSchema(
                    loader.ReadString(
                        "OpenTraceability",
                        "OpenTraceability.Utility.Data.DigitalLinkSchema.json")),

                [LinksetSchemaKey] = BuildSchema(
                    loader.ReadString(
                        "OpenTraceability",
                        "OpenTraceability.Utility.Data.LinksetSchema.json")),

                [GdstSchemaKey] = BuildSchema(
                    loader.ReadString(
                        "OpenTraceability",
                        "OpenTraceability.Utility.Data.gdst_json_schema.json")),

                [EpcisBaseSchemaKey] = BuildSchema(
                    loader.ReadString(
                        "OpenTraceability",
                        "OpenTraceability.Utility.Data.epcis_schema.json"))
            };
        }

        public static async Task<List<string>> IsValidAsync(string jsonStr, string schemaURL)
        {
            if (string.IsNullOrWhiteSpace(jsonStr))
                throw new ArgumentException("JSON cannot be null or empty.", nameof(jsonStr));

            if (string.IsNullOrWhiteSpace(schemaURL))
                throw new ArgumentException("Schema URL cannot be null or empty.", nameof(schemaURL));

            JsonSchema schema = await GetSchemaAsync(schemaURL).ConfigureAwait(false);

            using JsonDocument jDoc = JsonDocument.Parse(jsonStr);

            EvaluationResults results = schema.Evaluate(
                jDoc.RootElement,
                new EvaluationOptions
                {
                    OutputFormat = OutputFormat.List
                });

            if (results.IsValid)
                return new List<string>();

            IEnumerable<string> rootErrors =
                results.Errors?.Select(e => $"{e.Key} :: {e.Value}")
                ?? Enumerable.Empty<string>();

            IEnumerable<string> detailErrors =
                results.Details?
                    .SelectMany(GetErrorsRecursive)
                ?? Enumerable.Empty<string>();

            return rootErrors
                .Concat(detailErrors)
                .Distinct()
                .ToList();
        }

        /// <summary>
        /// Validates a document and reports each failure against the location that caused it.
        ///
        /// Unlike <see cref="IsValidAsync"/>, this keeps the instance location and the offending value
        /// that the validator calculates, grouping every reason under the element it belongs to.
        /// </summary>
        public static async Task<JsonSchemaValidationResult> ValidateAsync(string jsonStr, string schemaURL)
        {
            if (string.IsNullOrWhiteSpace(jsonStr))
                throw new ArgumentException("JSON cannot be null or empty.", nameof(jsonStr));

            if (string.IsNullOrWhiteSpace(schemaURL))
                throw new ArgumentException("Schema URL cannot be null or empty.", nameof(schemaURL));

            JsonSchema schema = await GetSchemaAsync(schemaURL).ConfigureAwait(false);

            using JsonDocument jDoc = JsonDocument.Parse(jsonStr);

            // The hierarchical format keeps the parent/child relationship between evaluation nodes.
            // The list format flattens it away, and that relationship is what tells a real failure
            // apart from a branch that did not matter.
            EvaluationResults results = schema.Evaluate(
                jDoc.RootElement,
                new EvaluationOptions
                {
                    OutputFormat = OutputFormat.Hierarchical
                });

            if (results.IsValid)
                return JsonSchemaValidationResult.Valid;

            var reasonsByLocation = new Dictionary<string, List<JsonSchemaValidationReason>>(StringComparer.Ordinal);
            var locationOrder = new List<string>();

            CollectReasons(results, reasonsByLocation, locationOrder);

            var errors = new List<JsonSchemaValidationError>(locationOrder.Count);

            foreach (string location in locationOrder)
            {
                errors.Add(new JsonSchemaValidationError(
                    location,
                    ReadValueAt(jDoc.RootElement, location),
                    reasonsByLocation[location]));
            }

            return new JsonSchemaValidationResult(errors, errors.Count);
        }

        private static void CollectReasons(
            EvaluationResults node,
            Dictionary<string, List<JsonSchemaValidationReason>> reasonsByLocation,
            List<string> locationOrder)
        {
            if (node.Errors != null && node.Errors.Count > 0)
            {
                string location = node.InstanceLocation.ToString();

                if (!reasonsByLocation.TryGetValue(location, out List<JsonSchemaValidationReason> reasons))
                {
                    reasons = new List<JsonSchemaValidationReason>();
                    reasonsByLocation[location] = reasons;
                    locationOrder.Add(location);
                }

                foreach (var error in node.Errors)
                {
                    reasons.Add(new JsonSchemaValidationReason(error.Key, error.Value));
                }
            }

            if (node.Details == null)
                return;

            foreach (EvaluationResults child in node.Details)
            {
                CollectReasons(child, reasonsByLocation, locationOrder);
            }
        }

        /// <summary>
        /// Resolves a JSON Pointer against the document and returns the value as text. Returns null
        /// for the root, for objects and arrays, or when the pointer does not resolve.
        /// </summary>
        private static string? ReadValueAt(JsonElement root, string pointer)
        {
            if (string.IsNullOrEmpty(pointer))
                return null;

            JsonElement current = root;

            foreach (string rawSegment in pointer.Split('/'))
            {
                if (rawSegment.Length == 0)
                    continue;

                // JSON Pointer escaping: "~1" is a slash, "~0" is a tilde. Order matters.
                string segment = rawSegment.Replace("~1", "/").Replace("~0", "~");

                if (current.ValueKind == JsonValueKind.Object)
                {
                    if (!current.TryGetProperty(segment, out JsonElement property))
                        return null;

                    current = property;
                }
                else if (current.ValueKind == JsonValueKind.Array && int.TryParse(segment, out int index))
                {
                    if (index < 0 || index >= current.GetArrayLength())
                        return null;

                    current = current.EnumerateArray().ElementAt(index);
                }
                else
                {
                    return null;
                }
            }

            if (current.ValueKind == JsonValueKind.Object || current.ValueKind == JsonValueKind.Array)
                return null;

            return current.ToString();
        }

        private static Task<JsonSchema> GetSchemaAsync(string schemaURL)
        {
            if (_builtInSchemas.TryGetValue(schemaURL, out JsonSchema builtInSchema))
                return Task.FromResult(builtInSchema);

            Lazy<Task<JsonSchema>> lazySchema = _remoteSchemaCache.GetOrAdd(
                schemaURL,
                key => new Lazy<Task<JsonSchema>>(
                    () => DownloadAndBuildSchemaAsync(key),
                    LazyThreadSafetyMode.ExecutionAndPublication));

            return lazySchema.Value;
        }

        private static async Task<JsonSchema> DownloadAndBuildSchemaAsync(string schemaURL)
        {
            string schemaStr = await _httpClient
                .GetStringAsync(schemaURL)
                .ConfigureAwait(false);

            return BuildSchema(schemaStr);
        }

        private static JsonSchema BuildSchema(string schemaStr)
        {
            var buildOptions = new BuildOptions
            {
                // Fresh registry per built schema avoids:
                // JsonSchemaException: Overwriting registered schemas is not permitted.
                SchemaRegistry = new SchemaRegistry()
            };

            return JsonSchema.FromText(schemaStr, buildOptions);
        }

        private static IEnumerable<string> GetErrorsRecursive(EvaluationResults result)
        {
            IEnumerable<string> ownErrors =
                result.Errors?.Select(e => $"{e.Key} :: {e.Value}")
                ?? Enumerable.Empty<string>();

            IEnumerable<string> childErrors =
                result.Details?.SelectMany(GetErrorsRecursive)
                ?? Enumerable.Empty<string>();

            return ownErrors.Concat(childErrors);
        }
    }
}