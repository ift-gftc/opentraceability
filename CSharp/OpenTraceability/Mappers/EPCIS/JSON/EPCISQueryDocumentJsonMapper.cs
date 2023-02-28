using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using OpenTraceability.Models.Events;
using OpenTraceability.Utility;
using System.Xml.Linq;

namespace OpenTraceability.Mappers.EPCIS.JSON
{
    public class EPCISQueryDocumentJsonMapper : IEPCISQueryDocumentMapper
    {
        public EPCISQueryDocument Map(string strValue)
        {
            try
            {
                EPCISQueryDocument doc = EPCISDocumentBaseJsonMapper.ReadJSON<EPCISQueryDocument>(strValue, out JObject json);

                if (doc.EPCISVersion != EPCISVersion.V2)
                {
                    throw new Exception("doc.EPCISVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.");
                }

                // read the query name
                doc.QueryName = json["epcisBody"]?["queryResults"]?["queryName"]?.ToString() ?? string.Empty;
                doc.SubscriptionID = json["epcisBody"]?["queryResults"]?["subscriptionID"]?.ToString() ?? string.Empty;

                // read the events
                JArray? jEventsList = json["epcisBody"]?["queryResults"]?["resultsBody"]?["eventList"] as JArray;
                if (jEventsList != null)
                {
                    foreach (JObject jEvent in jEventsList)
                    {
                        Type eventType = EPCISDocumentBaseJsonMapper.GetEventTypeFromProfile(jEvent);
                        IEvent e = (IEvent)OpenTraceabilityJsonLDMapper.FromJson(jEvent, eventType, doc.Namespaces);
                        doc.Events.Add(e);
                    }
                }

                return doc;
            }
            catch (Exception Ex)
            {
                System.Exception exception = new Exception("Failed to parse the EPCIS document from the XML. xml=" + strValue, Ex);
                OTLogger.Error(exception);
                throw;
            }
        }

        public string Map(EPCISQueryDocument doc)
        {
            if (doc.EPCISVersion != EPCISVersion.V2)
            {
                throw new Exception("doc.EPCISVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.");
            }

            XNamespace epcisNS = (doc.EPCISVersion == EPCISVersion.V2) ? Constants.EPCISQUERY_2_NAMESPACE : Constants.EPCISQUERY_1_NAMESPACE;

            var namespacesReversed = doc.Namespaces.Reverse();

            // write the events
            JArray jEventsList = new JArray();
            foreach (IEvent e in doc.Events)
            {
                JObject? jEvent = OpenTraceabilityJsonLDMapper.ToJson(e, namespacesReversed) as JObject;
                if (jEvent != null)
                {
                    jEventsList.Add(jEvent);
                }
            }

            JObject json = EPCISDocumentBaseJsonMapper.WriteJson(doc, epcisNS, "EPCISQueryDocument");

            JObject jEPCISBody = new JObject();
            JObject jQueryResults = new JObject();
            JObject jResultsBody = new JObject();

            jQueryResults["queryName"] = doc.QueryName;
            jQueryResults["subscriptionID"] = doc.SubscriptionID;

            jResultsBody["eventList"] = jEventsList;
            jQueryResults["resultsBody"] = jResultsBody;
            jEPCISBody["queryResults"] = jQueryResults;
            json["epcisBody"] = jEPCISBody;

            // conform the JSON-LD to the compacted version with CURIE's that EPCIS 2.0 likes
            EPCISDocumentBaseJsonMapper.ConformEPCISJsonLD(json, doc.Namespaces);

            // validate the JSON-LD schema
            EPCISDocumentBaseJsonMapper.CheckSchema(json);

            return json.ToString();
        }
    }
}