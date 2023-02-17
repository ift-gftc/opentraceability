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

namespace OpenTraceability.Mappers.EPCIS.JSON
{
    public class EPCISQueryDocumentJsonMapper : IEPCISQueryDocumentMapper
    {
        public EPCISQueryDocument Map(string strValue)
        {
            try
            {
                // TODO: validate the JSON schema

                EPCISQueryDocument doc = EPCISDocumentBaseJsonMapper.ReadJSon<EPCISQueryDocument>(strValue, out JObject json);

                if (doc.EPCISVersion != EPCISVersion.V2)
                {
                    throw new Exception("doc.EPCISVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.");
                }

                // read the query name
                doc.QueryName = json["epcisBody"]?["queryResults"]?["queryName"]?.ToString() ?? string.Empty;

                // read the events
                JArray? jEventsList = json["epcisBody"]?["queryResults"]?["resultsBody"]?["eventList"] as JArray;
                if (jEventsList != null)
                {
                    foreach (JObject jEvent in jEventsList)
                    {
                        // expand the CURIE's (bizStep, disposition, source, etc...)
                        EPCISDocumentBaseJsonMapper.ExpandCURIEsIntoFullURIs(jEvent);

                        Type eventType = EPCISDocumentBaseJsonMapper.GetEventTypeFromProfile(jEvent);
                        IEvent e = (IEvent)OpenTraceabilityJsonLDMapper.FromJson(jEvent, eventType);
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

            JObject json = EPCISDocumentBaseJsonMapper.WriteJson(doc, epcisNS, "EPCISQueryDocument");

            JObject jEPCISBody = new JObject();
            JObject jQueryResults = new JObject();
            JObject jResultsBody = new JObject();

            jQueryResults["queryName"] = doc.QueryName;

            // write the events
            JArray jEventsList = new JArray();
            foreach (IEvent e in doc.Events)
            {
                string xname = EPCISDocumentBaseJsonMapper.GetEventType(e);
                JObject? jEvent = OpenTraceabilityJsonLDMapper.ToJson(xname, e) as JObject;
                if (jEvent != null)
                {
                    jEventsList.Add(jEvent);
                }
            }


            jResultsBody["eventsList"] = jEventsList;
            jQueryResults["resultsBody"] = jResultsBody;
            jEPCISBody["queryResults"] = jQueryResults;
            json["epcisBody"] = jEPCISBody;

            // TODO: validate the json-ld schema

            return json.ToString();
        }
    }
}
