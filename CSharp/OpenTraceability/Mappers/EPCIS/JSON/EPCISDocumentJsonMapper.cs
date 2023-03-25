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
    public class EPCISDocumentJsonMapper : IEPCISDocumentMapper
    {
        public EPCISDocument Map(string strValue)
        {
            try
            {
                EPCISDocument doc = EPCISDocumentBaseJsonMapper.ReadJSON<EPCISDocument>(strValue, out JObject json);

                if (doc.EPCISVersion != EPCISVersion.V2)
                {
                    throw new Exception("doc.EPCISVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.");
                }

                // read the master data
                JObject? jMasterData = json["epcisHeader"]?["epcisMasterData"] as JObject;
                if (jMasterData != null)
                {
                    EPCISJsonMasterDataReader.ReadMasterData(doc, jMasterData);
                }

                // read the events
                JArray? jEventList = json["epcisBody"]?["eventList"] as JArray;
                if (jEventList != null)
                {
                    foreach (JObject jEvent in jEventList)
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

            var namespacesReversed = doc.Namespaces.Reverse();

            // write the events
            JArray jEventList = new JArray();
            JObject jEventBody = new JObject();
            jEventBody["eventList"] = jEventList;
            foreach (IEvent e in doc.Events)
            {
                JToken? jEvent = OpenTraceabilityJsonLDMapper.ToJson(e, namespacesReversed);
                if (jEvent != null)
                {
                    jEventList.Add(jEvent);
                }
            }

            JObject json = EPCISDocumentBaseJsonMapper.WriteJson(doc, epcisNS, "EPCISDocument");

            // write the header
            if (!string.IsNullOrWhiteSpace(doc.Header?.Sender?.Identifier))
            {
                json["sender"] = doc.Header.Sender.Identifier;
            }

            if (!string.IsNullOrWhiteSpace(doc.Header?.Receiver?.Identifier))
            {
                json["receiver"] = doc.Header.Receiver.Identifier;
            }

            if (!string.IsNullOrWhiteSpace(doc.Header?.DocumentIdentification?.InstanceIdentifier))
            {
                json["instanceIdentifier"] = doc.Header.DocumentIdentification.InstanceIdentifier;
            }

            EPCISJsonMasterDataWriter.WriteMasterData(json, doc);

            json["epcisBody"] = jEventBody;

            // conform the JSON-LD to the compacted version with CURIE's that EPCIS 2.0 likes
            EPCISDocumentBaseJsonMapper.ConformEPCISJsonLD(json, doc.Namespaces);

            // validate the JSON-LD schema
            EPCISDocumentBaseJsonMapper.CheckSchema(json);

            return json.ToString(Newtonsoft.Json.Formatting.Indented);
        }
    }
}
