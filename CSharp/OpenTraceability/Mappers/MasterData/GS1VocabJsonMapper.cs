using System;
using System.Collections.Generic;
using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using OpenTraceability.Utility;

namespace OpenTraceability.Mappers.MasterData
{
	public class GS1VocabJsonMapper : IMasterDataMapper
    {
        public string Map(IVocabularyElement vocab)
        {
            if (vocab.Context == null)
            {
                vocab.Context = JObject.Parse(@"{
                                    ""cbvmda"": ""urn:epcglobal:cbv:mda"",
                                    ""xsd"": ""http://www.w3.org/2001/XMLSchema#"",
                                    ""gs1"": ""http://gs1.org/voc/"",
                                    ""@vocab"": ""http://gs1.org/voc/"",
                                    ""gdst"": ""https://traceability-dialogue.org/vocab""
                                }"); 
            }

            Dictionary<string, string> namespaces = GetNamespaces(vocab.Context ?? throw new Exception("vocab.Context is null."));
            JObject json = OpenTraceabilityJsonLDMapper.ToJson(vocab, namespaces.Reverse()) as JObject ?? throw new Exception("Failed to map master data into GS1 web vocab.");
            json.AddFirst(new JProperty("@context", vocab.Context));
            return json.ToString();
        }

        public IVocabularyElement Map<T>(string value) where T : IVocabularyElement
        {
            return Map(typeof(T), value);
        }

        public IVocabularyElement Map(Type type, string value)
        {
            JObject json = JObject.Parse(value);
            Dictionary<string, string> namespaces = GetNamespaces(json["@context"] ?? throw new Exception("@context is null on the JSON-LD when deserializing GS1 Web Vocab. " + value));
            IVocabularyElement obj = (IVocabularyElement)OpenTraceabilityJsonLDMapper.FromJson(json, type, namespaces);
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

