using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using OpenTraceability.Models.Events;
using OpenTraceability.Utility;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Xml.Linq;

namespace OpenTraceability.Mappers.EPCIS.JSON
{
    public static class EPCISDocumentBaseJsonMapper
    {
        public static async Task<(T, JObject)> ReadJSONAsync<T>(string strValue, string expectedType, bool checkSchema = true) where T : EPCISBaseDocument, new()
        {
            // validate the JSON...
            if (checkSchema)
            {
                await CheckSchemaAsync(JObject.Parse(strValue));
            }

            // normalize the json-ld
            strValue = await NormalizeEPCISJsonLDAsync(strValue);

            // convert into XDocument
            var settings = new JsonSerializerSettings { DateParseHandling = DateParseHandling.None };
            JObject json = JsonConvert.DeserializeObject<JObject>(strValue, settings) ?? throw new Exception("Failed to parse json from string. " + strValue);

            if (json["type"]?.ToString() != expectedType)
            {
                throw new Exception("Failed to parse json from string. Expected type=" + expectedType + ", actual type=" + json["type"]?.ToString() ?? string.Empty);
            }

            // read all of the attributes
            T document = Activator.CreateInstance<T>();

            document.Attributes.Add("schemaVersion", json["schemaVersion"]?.ToString() ?? string.Empty);
            document.EPCISVersion = EPCISVersion.V2;

            // read the creation date
            string creationDateAttributeStr = json["creationDate"]?.ToString();
            if (!string.IsNullOrWhiteSpace(creationDateAttributeStr))
            {
                document.CreationDate = creationDateAttributeStr.TryConvertToDateTimeOffset();
            }

            // read the content...
            document.Attributes = new Dictionary<string, string>();

            // we are going to break down the content into either namespaces, or links to contexts...
            JArray jContextArray = json["@context"] as JArray;
            if (jContextArray != null)
            {
                foreach (JToken jt in jContextArray)
                {
                    // go through each item in the array...
                    if (jt is JObject)
                    {
                        // grab all namespaces from the jobject
                        JObject jobj = (JObject)jt;
                        var ns = JsonContextHelper.ScrapeNamespaces(jobj);
                        foreach (var n in ns)
                        {
                            if (!document.Namespaces.ContainsKey(n.Key))
                            {
                                document.Namespaces.Add(n.Key, n.Value);
                            }
                        }

                        // add it to the contexts..
                        document.Contexts.Add(jobj.ToString());
                    }
                    else
                    {
                        string val = jt.ToString();

                        if (!string.IsNullOrWhiteSpace(val))
                        {
                            // if this is a URL, then resolve it and grab the namespaces...
                            JObject jcontext = await JsonContextHelper.GetJsonLDContextAsync(val);
                            var ns = JsonContextHelper.ScrapeNamespaces(jcontext);
                            foreach (var n in ns)
                            {
                                if (!document.Namespaces.ContainsKey(n.Key))
                                {
                                    document.Namespaces.Add(n.Key, n.Value);
                                }
                            }

                            document.Contexts.Add(val);
                        }
                    }
                }
            }
            else throw new Exception("the @context on the root of the JSON-LD EPCIS file was not an array. we are currently expecting this to be an array.");

            if (json["id"] != null)
            {
                document.Attributes.Add("id", json.Value<string>("id") ?? string.Empty);
            }

            // read header information
            document.Header = new Models.Common.StandardBusinessDocumentHeader();

            document.Header.Sender = new Models.Common.SBDHOrganization();
            document.Header.Sender.Identifier = json["sender"]?.ToString() ?? string.Empty;

            document.Header.Receiver = new Models.Common.SBDHOrganization();
            document.Header.Receiver.Identifier = json["receiver"]?.ToString() ?? string.Empty;

            document.Header.DocumentIdentification = new Models.Common.SBDHDocumentIdentification();
            document.Header.DocumentIdentification.InstanceIdentifier = json["instanceIdentifier"]?.ToString() ?? string.Empty;

            return (document, json);
        }

        public static async Task<JObject> WriteJsonAsync(EPCISBaseDocument doc, XNamespace epcisNS, string docType)
        {
            if (doc.EPCISVersion != EPCISVersion.V2)
            {
                throw new Exception("doc.EPCISVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.");
            }

            // create a new xdocument with all of the namespaces...
            JObject json = new JObject();

            // write the context
            JArray jContext = new JArray();

            if (!doc.Contexts.Contains("https://ref.gs1.org/standards/epcis/epcis-context.jsonld") && !doc.Contexts.Contains("https://gs1.github.io/EPCIS/epcis-context.jsonld"))
            {
                doc.Contexts.Add("https://ref.gs1.org/standards/epcis/epcis-context.jsonld");
            }

            List<string> namespacesAlreadyWritten = new List<string>();
            foreach (string context in doc.Contexts)
            {
                if (Uri.TryCreate(context, UriKind.Absolute, out var uri))
                {
                    JObject jc = await JsonContextHelper.GetJsonLDContextAsync(context);
                    var ns = JsonContextHelper.ScrapeNamespaces(jc);
                    foreach (var n in ns)
                    {
                        if (doc.Namespaces.ContainsKey(n.Key))
                            doc.Namespaces.Remove(n.Key);
                    }

                    jContext.Add(JToken.FromObject(context));
                }
                else
                {
                    JObject jobj = JObject.Parse(context);
                    if (JsonContextHelper.IsComplexContext(jobj))
                    {
                        var ns = JsonContextHelper.ScrapeNamespaces(jobj);
                        foreach (var kvp in ns)
                        {
                            namespacesAlreadyWritten.Add(kvp.Value);
                        }

                        jContext.Add(jobj);
                    }
                }
            }

            foreach (var ns in doc.Namespaces)
            {
                if (!namespacesAlreadyWritten.Contains(ns.Value))
                {
                    JObject j = new JObject();
                    j[ns.Key] = ns.Value;
                    jContext.Add(j);
                }
            }

            json["@context"] = jContext;

            // write the type
            json["type"] = docType;

            // set the creation date
            if (doc.CreationDate != null)
            {
                json["creationDate"] = doc.CreationDate.Value.ToString("O");
            }

            json["schemaVersion"] = "2.0";

            // extra attributes
            if (doc.Attributes.ContainsKey("id"))
            {
                json["id"] = doc.Attributes["id"];
            }

            return json;
        }

        /// <summary>
        /// This performs a final cleanup on the JSON-LD document.
        /// </summary>
        /// <param name="json"></param>
        public static void PostWriteEventCleanUp(JObject json)
        {
            // when converting from XML to JSON, the XML allows an empty readPoint, but the JSON does not.
            if (json["readPoint"] is JObject && json["readPoint"]?["id"] == null)
            {
                json.Remove("readPoint");
            }
        }

        public static Type GetEventTypeFromProfile(JObject jEvent)
        {
            Enum.TryParse<EventAction>(jEvent["action"]?.ToString(), out var action);
            string bizStep = jEvent["bizStep"]?.ToString();
            string eventType = jEvent["type"]?.ToString() ?? throw new Exception("type property not set on event " + jEvent.ToString());

            var profiles = Setup.Profiles.Where(p => p.EventType.ToString() == eventType && (p.Action == null || p.Action == action) && (p.BusinessStep == null || p.BusinessStep.ToLower() == bizStep?.ToLower())).OrderByDescending(p => p.SpecificityScore).ToList();
            if (profiles.Count() == 0)
            {
                throw new Exception("Failed to create event from profile. Type=" + eventType + " and BizStep=" + bizStep + " and Action=" + action);
            }
            else
            {
                foreach (var profile in profiles.Where(p => p.KDEProfiles != null).ToList())
                {
                    if (profile.KDEProfiles != null)
                    {
                        foreach (var kdeProfile in profile.KDEProfiles)
                        {
                            if (jEvent.QueryJPath(kdeProfile.JPath) == null)
                            {
                                profiles.Remove(profile);
                            }
                        }
                    }
                }

                if (profiles.Count() == 0)
                {
                    throw new Exception("Failed to create event from profile. Type=" + eventType + " and BizStep=" + bizStep + " and Action=" + action);
                }

                return profiles.First().EventClassType;
            }
        }

        internal static async Task CheckSchemaAsync(JObject json)
        {
            string jsonStr = json.ToString();
            List<string> errors = await JsonSchemaChecker.IsValidAsync(jsonStr, "https://ref.gs1.org/standards/epcis/epcis-json-schema.json");
            if (errors.Count > 0)
            {
                throw new OpenTraceabilitySchemaException("Failed to validate JSON schema with errors:\n" + string.Join("\n", errors) + "\n\n and json " + json.ToString(Formatting.Indented));
            }
        }

        internal static string GetEventType(IEvent e)
        {
            if (e.EventType == EventType.ObjectEvent)
            {
                return "ObjectEvent";
            }
            else if (e.EventType == EventType.TransformationEvent)
            {
                return "TransformationEvent";
            }
            else if (e.EventType == EventType.TransactionEvent)
            {
                return "TransactionEvent";
            }
            else if (e.EventType == EventType.AggregationEvent)
            {
                return "AggregationEvent";
            }
            else if (e.EventType == EventType.AssociationEvent)
            {
                return "AssociationEvent";
            }
            else
            {
                throw new Exception("Failed to determine the event type. Event C# type is " + e.GetType().FullName);
            }
        }

        /// <summary>
        /// This will take an EPCIS JSON-LD document and make sure that everything is set for
        /// it to pass the JSON schema for EPCIS 2.0. This includes expanding the CURIEs, etc.
        /// </summary>
        /// <param name="jEPCISStr"></param>
        /// <returns></returns>
        internal static void ConformEPCISJsonLD(JObject json, Dictionary<string, string> namespaces)
        {
            CompressVocab(json);
            //JObject jEPCISContext = JsonContextHelper.GetJsonLDContext("https://ref.gs1.org/standards/epcis/epcis-context.jsonld");

            //JArray? jEventList = json["epcisBody"]?["eventList"] as JArray;
            //if (jEventList == null)
            //{
            //    jEventList = json["epcisBody"]?["queryResults"]?["resultsBody"]?["eventList"] as JArray;
            //}
            //if (jEventList != null)
            //{
            //    foreach (JObject jEvent in jEventList)
            //    {
            //        JsonContextHelper.CompressVocab(jEvent, jEPCISContext, namespaces);
            //    }
            //}
        }

        private static JToken CompressVocab(JToken json)
        {
            if (json is JObject)
            {
                JObject jobj = (JObject)json;
                foreach (var jprop in jobj.Properties())
                {
                    JToken jvalue = jobj[jprop.Name];
                    if (jvalue is JObject)
                    {
                        json[jprop.Name] = CompressVocab((JObject)jvalue);
                    }
                    else if (jvalue is JArray)
                    {
                        JArray ja = (JArray)jvalue;
                        for (int i = 0; i < ja.Count; i++)
                        {
                            JToken jt = ja[i];
                            ja[i] = CompressVocab(jt);
                        }
                    }
                    else if (jvalue != null)
                    {
                        json[jprop.Name] = CompressVocab(jvalue);
                    }
                }
                return jobj;
            }
            else
            {
                string val = json.ToString();
                if (val != null)
                {
                    if (val.StartsWith("urn:epcglobal:cbv:btt:")
                     || val.StartsWith("urn:epcglobal:cbv:bizstep:")
                     || val.StartsWith("urn:epcglobal:cbv:sdt:")
                     || val.StartsWith("urn:epcglobal:cbv:disp:"))
                    {
                        val = val.Split(':').Last();
                        return JToken.FromObject(val);
                    }
                    else if (val.StartsWith("https://ref.gs1.org/cbv"))
                    {
                        val = val.Split('-').Last();
                        return JToken.FromObject(val);
                    }
                    else if (val.StartsWith("https://gs1.org/voc/"))
                    {
                        val = val.Split('/').Last();
                        return JToken.FromObject(val);
                    }
                }
                return json;
            }
        }

        /// <summary>
        /// This will take an EPCIS Query Document or an EPCIS Document in the JSON-LD format
        /// and it will normalize the document so that all of the CURIEs are expanded into full
        /// URIs and that the JSON-LD is compacted.
        /// https://ref.gs1.org/standards/epcis/epcis-context.jsonld
        /// </summary>
        internal static async Task<string> NormalizeEPCISJsonLDAsync(string jEPCISStr)
        {
            // convert into XDocument
            var settings = new JsonSerializerSettings { DateParseHandling = DateParseHandling.None };
            JObject json = JsonConvert.DeserializeObject<JObject>(jEPCISStr, settings) ?? throw new Exception("Failed to parse json from string. " + jEPCISStr);

            JObject jEPCISContext = await JsonContextHelper.GetJsonLDContextAsync("https://ref.gs1.org/standards/epcis/epcis-context.jsonld");
            Dictionary<string, string> namespaces = JsonContextHelper.ScrapeNamespaces(jEPCISContext);

            JArray jEventList = json["epcisBody"]?["eventList"] as JArray;
            if (jEventList == null)
            {
                jEventList = json["epcisBody"]?["queryResults"]?["resultsBody"]?["eventList"] as JArray;
            }
            if (jEventList != null)
            {
                foreach (JObject jEvent in jEventList)
                {
                    JsonContextHelper.ExpandVocab(jEvent, jEPCISContext, namespaces);
                }
            }

            return json.ToString(Formatting.Indented);
        }
    }
}