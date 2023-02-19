using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;
using Microsoft.VisualBasic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using OpenTraceability.Mappers.EPCIS.XML;
using OpenTraceability.Models.Events;
using OpenTraceability.Utility;

namespace OpenTraceability.Mappers.EPCIS.JSON
{
    public static class EPCISDocumentBaseJsonMapper
    {
        static JObject? jEPCISContext = null;

        public static T ReadJSon<T>(string strValue, out JObject json) where T : EPCISBaseDocument, new()
        {
            // convert into XDocument
            var settings = new JsonSerializerSettings { DateParseHandling = DateParseHandling.None };
            json = JsonConvert.DeserializeObject<JObject>(strValue, settings) ?? throw new Exception("Failed to parse json from string. " + strValue);

            // read all of the attributes
            T document = Activator.CreateInstance<T>();

            document.Attributes.Add("schemaVersion", json["schemaVersion"]?.ToString() ?? string.Empty);
            document.EPCISVersion = EPCISVersion.V2;

            // read the creation date
            string? creationDateAttributeStr = json["creationDate"]?.ToString();
            if (!string.IsNullOrWhiteSpace(creationDateAttributeStr))
            {
                document.CreationDate = creationDateAttributeStr.TryConvertToDateTimeOffset();
            }

            return document;
        }

        public static JObject WriteJson(EPCISBaseDocument doc, XNamespace epcisNS, string rootEleName)
        {
            if (doc.EPCISVersion != EPCISVersion.V2)
            {
                throw new Exception("doc.EPCISVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.");
            }

            // create a new xdocument with all of the namespaces...
            JObject json = new JObject();
            json["type"] = rootEleName;

            // set the creation date
            if (doc.CreationDate != null)
            {
                json["creationDate"] = doc.CreationDate.Value.ToString("O");
            }

            json["schemaVersion"] = "2.0";

            return json;
        }

        internal static Type GetEventTypeFromProfile(JObject jEvent)
        {
            Enum.TryParse<EventAction>(jEvent["action"]?.ToString(), out var action);
            string? bizStep = jEvent["bizStep"]?.ToString();
            string eventType = jEvent["type"]?.ToString() ?? throw new Exception("type property not set on event " + jEvent.ToString());

            OpenTraceabilityEventProfile? profile = OpenTraceability.Profiles.Where(p => p.EventType == eventType && (p.Action == null || p.Action == action) && (p.BusinessStep == null || p.BusinessStep == bizStep)).OrderByDescending(p => p.SpecificityScore).FirstOrDefault();
            if (profile == null)
            {
                throw new Exception("Failed to create event from profile. Type=" + eventType + " and BizStep=" + bizStep + " and Action=" + action);
            }
            else
            {
                return profile.EventClassType;
            }
        }

        internal static string GetEventType(IEvent e)
        {
            if (e.EventType == EventType.Object)
            {
                return "ObjectEvent";
            }
            else if (e.EventType == EventType.Transformation)
            {
                return "TransformationEvent";
            }
            else if (e.EventType == EventType.Transaction)
            {
                return "TransactionEvent";
            }
            else if (e.EventType == EventType.Aggregation)
            {
                return "AggregationEvent";
            }
            else if (e.EventType == EventType.Association)
            {
                return "AssociationEvent";
            }
            else
            {
                throw new Exception("Failed to determine the event type. Event C# type is " + e.GetType().FullName);
            }
        }

        /// <summary>
        /// This will convert all CURIEs into full URIs using the https://ref.gs1.org/standards/epcis/epcis-context.jsonld
        /// </summary>
        /// <param name="jEvent"></param>
        /// <returns></returns>
        internal static void ExpandCURIEsIntoFullURIs(JObject jEvent)
        {
            if (jEPCISContext == null)
            {
                using (var wc = new HttpClient())
                {
                    jEPCISContext = JObject.Parse(wc.GetStringAsync("https://ref.gs1.org/standards/epcis/epcis-context.jsonld").Result);
                }
            }

            // grab all of the namespaces
            Dictionary<string, string> namespaces = new Dictionary<string, string>();
            JObject jContext = jEPCISContext["@context"] as JObject ?? throw new Exception("Failed to grab the @context. " + jEPCISContext.ToString());
            foreach (JProperty jprop in jContext.Properties())
            {
                if (jContext[jprop.Name] is JObject)
                {
                    continue;
                }
                string? value = jContext[jprop.Name]?.ToString();
                if (value != null)
                {
                    if (Uri.TryCreate(value, UriKind.Absolute, out Uri? uriResult) && (uriResult.Scheme == Uri.UriSchemeHttp || uriResult.Scheme == Uri.UriSchemeHttps))
                    {
                        namespaces.Add(jprop.Name, value);
                    }
                }
            }

            // we will go through each property on the jEvent...
            ExpandCURIEsIntoFullURIs_Internal(jEvent, jContext, namespaces);
        }

        private static void ExpandCURIEsIntoFullURIs_Internal(JObject json, JObject jContext, Dictionary<string, string> namespaces)
        {
            // we will go through each property on the jEvent...
            foreach (JProperty jprop in json.Properties())
            {
                JObject? jContextProp = jContext[jprop.Name] as JObject;
                if (jContextProp != null)
                {
                    if (jContextProp["@container"]?.ToString() == "@set")
                    {
                        // we are looking at expanding a list
                        JToken jpropvalue = json[jprop.Name];
                        if (jpropvalue != null)
                        {
                            if (jpropvalue is JArray)
                            {
                                JArray? jArr = json[jprop.Name] as JArray;
                                if (jArr != null)
                                {
                                    JObject? jchildcontext = jContextProp["@context"] as JObject;
                                    if (jchildcontext != null)
                                    {
                                        for (int i = 0; i < jArr.Count; i++) 
                                        {
                                            JToken j = jArr[i];
                                            if (j is JObject)
                                            {
                                                ExpandCURIEsIntoFullURIs_Internal(j as JObject, jchildcontext, namespaces);
                                            }
                                            else
                                            {
                                                JToken? jmapping = jContextProp["@context"]?[j.ToString()];
                                                if (jmapping != null)
                                                {
                                                    string uri = jmapping.ToString();
                                                    string[] parts = uri.Split(':');
                                                    string ns = namespaces[parts[0]];
                                                    jArr[i] = ns + parts[1];
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else if (jpropvalue is JObject)
                            {
                                JArray? jchildcontext = jContextProp["@context"] as JArray;
                                if (jchildcontext != null)
                                {
                                    JObject jnewcontext = jchildcontext[1] as JObject;
                                    foreach (JProperty jnewprop in jnewcontext.Properties())
                                    {
                                        if (jnewcontext[jnewprop.Name] is JObject)
                                        {
                                            jnewcontext[jnewprop.Name]["@context"] = jchildcontext[0];
                                        }
                                    }
                                    ExpandCURIEsIntoFullURIs_Internal(jpropvalue as JObject, jnewcontext, namespaces);
                                }
                            }
                        }
                    }
                    else if (jContextProp["@type"]?.ToString() == "@vocab")
                    {
                        // we will expand a single value
                        string? value = json[jprop.Name]?.Value<string>();
                        if (value != null)
                        {
                            JToken? jmapping = jContextProp["@context"]?[value];
                            if (jmapping != null)
                            {
                                string uri = jmapping.ToString();
                                string[] parts = uri.Split(':');
                                string ns = namespaces[parts[0]];
                                json[jprop.Name] = ns + parts[1];
                            }
                        }
                    }
                }
            }
        }
    }
}
