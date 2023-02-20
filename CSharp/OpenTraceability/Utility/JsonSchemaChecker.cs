using Newtonsoft.Json.Linq;
using Newtonsoft.Json.Schema;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Utility
{
    public static class JsonSchemaChecker
    {
        static object _lock = new object();
        static ConcurrentDictionary<string, string> _schemaCache = new ConcurrentDictionary<string, string>();

        public static bool IsValid(JObject json, string schemaURL, out List<string> errors)
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

            JSchema schema = JSchema.Parse(schemaStr);

            bool isvalid = json.IsValid(schema, out IList<string> e);
            errors = e.ToList();
            return isvalid;
        }
    }
}
