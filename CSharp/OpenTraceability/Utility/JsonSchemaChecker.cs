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