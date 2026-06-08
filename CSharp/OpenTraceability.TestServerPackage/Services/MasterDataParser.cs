using System;
using System.Collections.Generic;
using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using OpenTraceability.Mappers;

namespace OpenTraceability.TestServer.Core.Services
{
    /// <summary>
    /// Parses GS1 Web Vocab JSON-LD master data into the correct (possibly GDST-extended) vocabulary
    /// type by inspecting the JSON-LD <c>@type</c> and registered master data types.
    /// </summary>
    public static class MasterDataParser
    {
        private const string DefaultContext = @"{
            ""cbvmda"": ""urn:epcglobal:cbvmda:mda"",
            ""xsd"": ""http://www.w3.org/2001/XMLSchema#"",
            ""gs1"": ""http://gs1.org/voc/"",
            ""@vocab"": ""http://gs1.org/voc/"",
            ""gdst"": ""https://traceability-dialogue.org/vocab""
        }";

        /// <summary>
        /// Parses a posted master data body that is either a single JSON-LD object or an array of them.
        /// </summary>
        public static List<IVocabularyElement> ParseMany(string json)
        {
            var results = new List<IVocabularyElement>();
            JToken token = JToken.Parse(json);

            if (token is JArray array)
            {
                foreach (var item in array)
                {
                    if (item is JObject obj && ParseObject(obj) is { } element)
                    {
                        results.Add(element);
                    }
                }
            }
            else if (token is JObject single)
            {
                // A GS1 vocab document may wrap items in "@graph".
                if (single["@graph"] is JArray graph)
                {
                    foreach (var item in graph)
                    {
                        if (item is JObject obj && ParseObject(obj) is { } element)
                        {
                            results.Add(element);
                        }
                    }
                }
                else if (ParseObject(single) is { } element)
                {
                    results.Add(element);
                }
            }

            return results;
        }

        private static IVocabularyElement? ParseObject(JObject obj)
        {
            // Ensure an @context exists so the GS1 vocab mapper can resolve namespaces.
            if (obj["@context"] == null)
            {
                obj["@context"] = JToken.Parse(DefaultContext);
            }

            string epcisType = DetermineEpcisType(obj);
            if (string.IsNullOrEmpty(epcisType)) return null;

            if (!OpenTraceability.Setup.MasterDataTypes.TryGetValue(epcisType.ToLower(), out Type? t) || t == null)
            {
                return null;
            }

            return OpenTraceabilityMappers.MasterData.GS1WebVocab.Map(t, obj.ToString());
        }

        private static string DetermineEpcisType(JObject obj)
        {
            string typeStr = (obj["@type"]?.ToString() ?? obj["type"]?.ToString() ?? string.Empty).ToLower();

            if (typeStr.Contains("product")) return "urn:epcglobal:epcis:vtype:EPCClass";
            if (typeStr.Contains("place")) return "urn:epcglobal:epcis:vtype:Location";
            if (typeStr.Contains("organization")) return "urn:epcglobal:epcis:vtype:Party";

            // Fall back to inferring from identifying properties.
            if (obj["gtin"] != null) return "urn:epcglobal:epcis:vtype:EPCClass";
            if (obj["globalLocationNumber"] != null && obj["organizationName"] == null) return "urn:epcglobal:epcis:vtype:Location";
            if (obj["organizationName"] != null) return "urn:epcglobal:epcis:vtype:Party";

            return string.Empty;
        }
    }
}
