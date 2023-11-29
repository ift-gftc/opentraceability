using Json.Schema;
using Nito.AsyncEx;
using System.Collections.Concurrent;
using System.Text.Json;

namespace OpenTraceability.Utility
{
    public static class JsonSchemaChecker
    {
        private static AsyncLock _lock = new AsyncLock();
        private static ConcurrentDictionary<string, string> _schemaCache = new ConcurrentDictionary<string, string>();

        public static async Task<List<string>> IsValidAsync(string jsonStr, string schemaURL)
        {
            List<string> errors = new List<string>();
            if (!_schemaCache.TryGetValue(schemaURL, out string? schemaStr))
            {
                using (await _lock.LockAsync())
                {
                    if (schemaURL == "https://ref.gs1.org/standards/epcis/epcis-json-schema.json")
                    {
                        EmbeddedResourceLoader loader = new EmbeddedResourceLoader();
                        schemaStr = loader.ReadString("OpenTraceability", "OpenTraceability.Utility.Data.EPCISJsonSchema.jsonld");
                        _schemaCache.TryAdd(schemaURL, schemaStr);
                    }
                    else
                    {
                        using (HttpClient client = new HttpClient())
                        {
                            schemaStr = await client.GetStringAsync(schemaURL);
                            _schemaCache.TryAdd(schemaURL, schemaStr);
                        }
                    }
                }
            }

            var jDoc = JsonDocument.Parse(jsonStr);
            var mySchema = JsonSchema.FromText(schemaStr);
            var results = mySchema.Evaluate(jDoc, new EvaluationOptions() { OutputFormat = OutputFormat.List });
            if (!results.IsValid)
            {
                errors = results.Errors?.Select(e => string.Format("{0} :: {1}", e.Key, e.Value)).ToList() ?? new List<string>();
                errors.AddRange(results.Details?.SelectMany(e => e.Errors ?? new Dictionary<string, string>()).Select(e => string.Format("{0} :: {1}", e.Key, e.Value)).ToList() ?? new List<string>());
            }

            return errors;
        }
    }
}