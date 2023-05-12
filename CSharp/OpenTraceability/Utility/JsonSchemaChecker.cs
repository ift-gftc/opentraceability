using Json.Schema;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Reflection.Metadata;
using System.Text;
using System.Text.Json;
using System.Threading.Tasks;

namespace OpenTraceability.Utility
{
    public static class JsonSchemaChecker
    {
        static object _lock = new object();
        static ConcurrentDictionary<string, string> _schemaCache = new ConcurrentDictionary<string, string>();

        public static bool IsValid(string jsonStr, string schemaURL, out List<string> errors)
        {
            if (!_schemaCache.TryGetValue(schemaURL, out string? schemaStr))
            {
                lock (_lock)
                {
                    using (HttpClient client = new HttpClient())
                    {
                        schemaStr = client.GetStringAsync(schemaURL).Result;
                        _schemaCache.TryAdd(schemaURL, schemaStr);
                    }
                }
            }

            var jDoc = JsonDocument.Parse(jsonStr);
            var mySchema = JsonSchema.FromText(schemaStr);
            var results = mySchema.Evaluate(jDoc, new EvaluationOptions() { OutputFormat = OutputFormat.List });
            if (results.IsValid)
            {
                errors = new List<string>();
                return true;
            }
            else
            {
                errors = results.Errors?.Select(e => string.Format("{0} :: {1}", e.Key, e.Value)).ToList() ?? new List<string>();
                errors.AddRange(results.Details?.SelectMany(e => e.Errors ?? new Dictionary<string,string>()).Select(e => string.Format("{0} :: {1}", e.Key, e.Value)).ToList() ?? new List<string>());
                return false;
            }
        }
    }
}
