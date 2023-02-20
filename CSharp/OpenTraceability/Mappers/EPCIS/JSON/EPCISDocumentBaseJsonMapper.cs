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

        public static T ReadJSON<T>(string strValue, out JObject json) where T : EPCISBaseDocument, new()
        {
            // normalize the json-ld
            strValue = OpenTraceabilityJsonLDMapper.NormalizeEPCISJsonLD(strValue);

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

            // read the contentt...
            document.Attributes = new Dictionary<string, string>();
            document.Attributes.Add("@context", json["@context"]?.ToString() ?? string.Empty);

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

            // write the context
            json["@context"] = JToken.Parse(doc.Attributes["@context"]);

            // extra attributes
            if (doc.Attributes.ContainsKey("id"))
            {
                json["id"] = doc.Attributes["id"];
            }

            // write the header
            if (doc.Header != null)
            {
                if (doc.Header.Sender != null)
                {
                    json["sender"] = doc.Header.Sender.Identifier;
                }

                if (doc.Header.Receiver != null)
                {
                    json["receiver"] = doc.Header.Receiver.Identifier;
                }

                if (doc.Header.DocumentIdentification != null)
                {
                    json["instanceIdentifier"] = doc.Header.DocumentIdentification.InstanceIdentifier;
                }
            }

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
    }
}
