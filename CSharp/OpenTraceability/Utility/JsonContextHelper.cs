using Newtonsoft.Json.Linq;
using Newtonsoft.Json.Schema;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using System.Xml.Linq;

namespace OpenTraceability.Utility
{
    internal enum JsonLDVocabTransformationType
    {
        Expand,
        Compress
    }

    /// <summary>
    /// A utility class for fetching a JSON-LD context file from a URL.
    /// </summary>
    public static class JsonContextHelper
    {
        static object _lock = new object();
        static ConcurrentDictionary<string, JObject> _contextCache = new ConcurrentDictionary<string, JObject>();

        public static JObject GetJsonLDContext(string contextURL)
        {
            if (!_contextCache.TryGetValue(contextURL, out JObject? jContext))
            {
                lock (_lock)
                {
                    using (HttpClient client = new HttpClient())
                    {
                        jContext = JObject.Parse(client.GetStringAsync(contextURL).Result);
                        _contextCache.TryAdd(contextURL, jContext);
                    }
                }
            }

            return jContext["@context"] as JObject ?? throw new Exception("Failed to fetched JSON-LD context from " + contextURL);
        }

        public static Dictionary<string, string> ScrapeNamespaces(JObject jContext)
        {
            Dictionary<string, string> namespaces = new Dictionary<string, string>();
            foreach (JProperty jprop in jContext.Properties())
            {
                if (jContext[jprop.Name] is JObject)
                {
                    continue;
                }
                string? value = jContext[jprop.Name]?.ToString();
                if (value != null && IsNamespace(value))
                {
                    namespaces.Add(jprop.Name, value.TrimEnd(':'));
                }
            }
            return namespaces;
        }

        public static bool IsNamespace(string value)
        {
            Regex reg = new Regex(@"^urn:[a-z0-9][a-z0-9-]{0,31}:[a-z0-9()+,\-.:=@;$_!*'%\/?#]+$");
            if (Uri.TryCreate(value, UriKind.Absolute, out Uri? uriResult) && (uriResult.Scheme == Uri.UriSchemeHttp || uriResult.Scheme == Uri.UriSchemeHttps))
            {
                return true;
            }
            else if (reg.IsMatch(value))
            {
                return true;
            }
            return false;
        }

        public static JToken? ExpandVocab(JToken json, JObject jcontext, Dictionary<string, string> namespaces, JObject? jvocabcontext=null)
        {
            return ModifyVocab(json, jcontext, namespaces, namespaces.Reverse(), JsonLDVocabTransformationType.Expand);
        }

        public static JToken? CompressVocab(JToken json, JObject jcontext, Dictionary<string, string> namespaces, JObject? jvocabcontext = null)
        {
            return ModifyVocab(json, jcontext, namespaces, namespaces.Reverse(), JsonLDVocabTransformationType.Compress);
        }

        private static JToken? ModifyVocab(JToken json, JObject jcontext, Dictionary<string, string> namespaces, Dictionary<string, string> namespacesReverse, JsonLDVocabTransformationType transformType, JObject? jvocabcontext = null)
        {
            if (json is JObject)
            {
                JObject jobj = (JObject)json;

                // we will go through each property on the jEvent...
                foreach (JProperty jprop in jobj.Properties())
                {
                    JObject? jcontextprop = jcontext[jprop.Name] as JObject;
                    if (jcontextprop != null)
                    {
                        JToken? jpropvalue = jobj[jprop.Name];
                        JToken? jpropcontext = jcontextprop["@context"] ?? jvocabcontext;
                        if (jpropvalue != null && jpropcontext != null)
                        {
                            if (jcontextprop.Value<string>("@type") == "@vocab")
                            {
                                if (jpropcontext is JObject)
                                {
                                    JObject jpropcontextObj = (JObject)jpropcontext;

                                    if (jpropvalue is JArray)
                                    {
                                        JArray jarr = (JArray)jpropvalue;
                                        for (int i = 0; i < jarr.Count; i++)
                                        {
                                            JToken jt = jarr[i];
                                            JToken? newValue = ModifyVocab(jt, jpropcontextObj, namespaces, namespacesReverse, transformType, jvocabcontext);
                                            if (newValue != null)
                                            {
                                                jarr[i] = newValue;
                                            }
                                        }
                                    }
                                    else
                                    {
                                        JToken? newValue = ModifyVocab(jpropvalue, jpropcontextObj, namespaces, namespacesReverse, transformType, jvocabcontext);
                                        if (newValue != null)
                                        {
                                            jobj[jprop.Name] = newValue;
                                        }
                                    }
                                }
                                else throw new Exception($"jcontextprop has @type set to @vocab, but the @context is not a JObject. jcontextprop={jcontextprop} and jpropvalue={jpropvalue}");
                            }
                            else if (jpropvalue is JArray)
                            {
                                JArray jarr = (JArray)jpropvalue;
                                JObject jpropcontextObj = (JObject)jpropcontext;
                                for (int i = 0; i < jarr.Count; i++)
                                {
                                    JToken jt = jarr[i];
                                    if (jt is JObject)
                                    {
                                        JObject jitem = (JObject)jt;
                                        ModifyVocab(jitem, jpropcontextObj, namespaces, namespacesReverse, transformType, jvocabcontext);
                                    }
                                    else
                                    {
                                        JToken? newValue = ModifyVocab(jt, jpropcontextObj, namespaces, namespacesReverse, transformType, jvocabcontext);
                                        if (newValue != null)
                                        {
                                            jarr[i] = newValue;
                                        }
                                    }
                                }
                            }
                            else if (jpropvalue is JObject)
                            {
                                JObject jpropcontextObj;
                                if (jpropcontext is JArray)
                                {
                                    jvocabcontext = (JObject)jpropcontext[0];
                                    jpropcontextObj = (JObject)jpropcontext[1];
                                }
                                else
                                {
                                    jvocabcontext = null;
                                    jpropcontextObj = (JObject)jpropcontext;
                                }

                                ModifyVocab(jpropvalue, jpropcontextObj, namespaces, namespacesReverse, transformType, jvocabcontext);
                            }
                        }
                    }
                }

                return jobj;
            }
            else
            {
                JObject jc = jvocabcontext ?? jcontext;
                string? value = json.ToString();

                if (value == null)
                {
                    return null;
                }
                else if (transformType == Utility.JsonLDVocabTransformationType.Expand)
                {
                    JToken? jmapping = jc[value];

                    if (jmapping != null)
                    {
                        string uri = jmapping.ToString();
                        string[] parts = uri.Split(':');
                        string ns = namespaces[parts[0]];
                        string newValue = ns + parts[1];
                        return JToken.FromObject(newValue);
                    }
                }
                else if (transformType == JsonLDVocabTransformationType.Compress)
                {
                    string ns = value.Substring(0, value.LastIndexOf('/') + 1);
                    if (namespacesReverse.ContainsKey(ns))
                    {
                        value = value.Replace(ns, namespacesReverse[ns] + ":");
                    }

                    string? compressedVocab = jc.Properties().FirstOrDefault(f => f.Value.ToString() == value)?.Name;
                    if (compressedVocab != null)
                    {
                        return JToken.FromObject(compressedVocab);
                    }
                }
                else
                {
                    throw new Exception("Unrecognized JSON-LD Vocab Transformation Type = " + transformType);
                }

                return null;
            }
        }

        // checks if this has properties that are not just namespaces...
        internal static bool IsComplexContext(JObject jobj)
        {
            foreach (JProperty jprop in jobj.Properties())
            {
                if (jobj[jprop.Name] is JObject)
                {
                    return true;
                }

                string? value = jobj[jprop.Name]?.ToString();
                if (value == null || !IsNamespace(value))
                {
                    return true;
                }
            }
            return false;
        }
    }
}
