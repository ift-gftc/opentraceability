using System;
using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using OpenTraceability.Utility;

namespace OpenTraceability.Mappers.MasterData
{
	public class GS1VocabJsonMapper : IMasterDataMapper
    {
        public string Map(IVocabularyElement vocab)
        {
            Dictionary<string, string> namespaces = GetNamespaces(vocab.Context ?? throw new Exception("vocab.Context is null."));
            JObject json = OpenTraceabilityJsonLDMapper.ToJson(vocab, namespaces.Reverse()) as JObject ?? throw new Exception("Failed to map master data into GS1 web vocab.");
            json["@context"] = vocab.Context;
            return json.ToString();
        }

        public IVocabularyElement Map<T>(string value) where T : IVocabularyElement
        {
            JObject json = JObject.Parse(value);
            Dictionary<string, string> namespaces = GetNamespaces(json["@context"] ?? throw new Exception("@context is null on the JSON-LD when deserializing GS1 Web Vocab. " + value));
            T obj = OpenTraceabilityJsonLDMapper.FromJson<T>(json, namespaces);
            obj.Context = json["@context"];
            return obj;
        }

        private Dictionary<string, string> GetNamespaces(JToken jContext)
        {
            // build our namespaces
            Dictionary<string, string> namespaces = new Dictionary<string, string>();
            if (jContext != null)
            {
                if (jContext is JObject)
                {
                    namespaces = JsonContextHelper.ScrapeNamespaces((JObject)jContext);
                }
                else if (jContext is JArray)
                {
                    foreach (JObject j in (JArray)jContext)
                    {
                        var ns = JsonContextHelper.ScrapeNamespaces(j);
                        foreach (var kvp in ns)
                        {
                            if (!namespaces.ContainsKey(kvp.Key))
                            {
                                namespaces.Add(kvp.Key, kvp.Value);
                            }
                        }
                    }
                }
            }
            return namespaces;
        }
    }
}

