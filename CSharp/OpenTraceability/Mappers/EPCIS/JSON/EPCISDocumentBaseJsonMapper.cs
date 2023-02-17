using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;
using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using OpenTraceability.Mappers.EPCIS.XML;
using OpenTraceability.Models.Events;
using OpenTraceability.Utility;

namespace OpenTraceability.Mappers.EPCIS.JSON
{
    public static class EPCISDocumentBaseJsonMapper
    {
        public static T ReadJSon<T>(string strValue, out JObject json) where T : EPCISBaseDocument, new()
        {
            // convert into XDocument
            json = JObject.Parse(strValue);

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
    }
}
