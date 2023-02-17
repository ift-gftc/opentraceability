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
    public class EPCISDocumentJsonMapper : IEPCISDocumentMapper
    {
        public EPCISDocument Map(string strValue)
        {
            try
            {
                EPCISDocument doc = EPCISDocumentBaseJsonMapper.ReadJSon<EPCISDocument>(strValue, out JObject json);

                if (doc.EPCISVersion != EPCISVersion.V2)
                {
                    throw new Exception("doc.EPCISVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.");
                }

                // TODO: validate json-ld schema

                // read the events
                JArray? jEventList = json["epcisBody"]?["eventList"] as JArray;
                if (jEventList != null)
                {
                    foreach (JObject jEvent in jEventList)
                    {
                        Type eventType = EPCISDocumentBaseJsonMapper.GetEventTypeFromProfile(jEvent);
                        IEvent e = (IEvent)OpenTraceabilityJsonLDMapper.FromJson(jEvent, eventType);
                        doc.Events.Add(e);
                    }
                }

                return doc;
            }
            catch (Exception Ex)
            {
                System.Exception exception = new Exception("Failed to parse the EPCIS Document from the JSON-LD. json-ld=" + strValue, Ex);
                OTLogger.Error(exception);
                throw;
            }
        }

        public string Map(EPCISDocument doc)
        {
            if (doc.EPCISVersion != EPCISVersion.V2)
            {
                throw new Exception("doc.EPCISVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.");
            }

            XNamespace epcisNS = (doc.EPCISVersion == EPCISVersion.V2) ? Constants.EPCIS_2_NAMESPACE : Constants.EPCIS_1_NAMESPACE;

            JObject json = EPCISDocumentBaseJsonMapper.WriteJson(doc, epcisNS, "EPCISDocument");

            // write the events
            JArray jEventList = new JArray();
            JObject jEventBody = new JObject();
            jEventBody["eventList"] = jEventList;
            json["epcisBody"] = jEventBody;
            foreach (IEvent e in doc.Events)
            {
                string xname = EPCISDocumentBaseXMLMapper.GetEventXName(e);
                JToken? jEvent = OpenTraceabilityJsonLDMapper.ToJson(xname, e);
                if (jEvent != null)
                {
                    jEventList.Add(jEvent);
                }
            }

            // TODO: validate the JSON-LD schema

            return json.ToString(Newtonsoft.Json.Formatting.Indented);
        }
    }
}
