package opentraceability.mappers.epcis.json;

import Newtonsoft.Json.Linq.*;
import opentraceability.interfaces.*;
import opentraceability.mappers.epcis.xml.*;
import opentraceability.models.events.*;
import opentraceability.utility.*;
import opentraceability.*;
import opentraceability.mappers.*;
import opentraceability.mappers.epcis.*;

public class EPCISDocumentJsonMapper implements IEPCISDocumentMapper
{
	public final EPCISDocument Map(String strValue)
	{
		return MapAsync(strValue).GetAwaiter().GetResult();
	}

	public final String Map(EPCISDocument doc)
	{
		return MapAsync(doc).GetAwaiter().GetResult();
	}

//C# TO JAVA CONVERTER TASK: There is no equivalent in Java to the 'async' keyword:
//ORIGINAL LINE: public async Task<EPCISDocument> MapAsync(string strValue)
	public final Task<EPCISDocument> MapAsync(String strValue)
	{
		try
		{
//C# TO JAVA CONVERTER TASK: Java has no equivalent to C# deconstruction declarations:
			(EPCISDocument doc, JObject json) = await EPCISDocumentBaseJsonMapper.<EPCISDocument>ReadJSONAsync(strValue, "EPCISDocument");

			if (doc.EPCISVersion != EPCISVersion.V2)
			{
				throw new RuntimeException("doc.EPCISVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.");
			}

			// read the master data
			JObject jMasterData = json["epcisHeader"] == null ? null : json["epcisHeader"]["epcisMasterData"] instanceof JObject ? (JObject)json["epcisHeader"]["epcisMasterData"] : null;
			if (jMasterData != null)
			{
				EPCISJsonMasterDataReader.ReadMasterData(doc, jMasterData);
			}

			// read the events
			JArray jEventList = json["epcisBody"] == null ? null : json["epcisBody"]["eventList"] instanceof JArray ? (JArray)json["epcisBody"]["eventList"] : null;
			if (jEventList != null)
			{
				for (JObject jEvent : jEventList)
				{
					java.lang.Class eventType = EPCISDocumentBaseJsonMapper.GetEventTypeFromProfile(jEvent);
					IEvent e = (IEvent)OpenTraceabilityJsonLDMapper.FromJson(jEvent, eventType, doc.Namespaces);
					doc.Events.Add(e);
				}
			}

			return doc;
		}
		catch (RuntimeException Ex)
		{
			RuntimeException exception = new RuntimeException("Failed to parse the EPCIS Document from the JSON-LD. json-ld=" + strValue, Ex);
			OTLogger.Error(exception);
			throw Ex;
		}
	}

//C# TO JAVA CONVERTER TASK: There is no equivalent in Java to the 'async' keyword:
//ORIGINAL LINE: public async Task<string> MapAsync(EPCISDocument doc)
	public final Task<String> MapAsync(EPCISDocument doc)
	{
		if (doc.getEPCISVersion() != EPCISVersion.V2)
		{
			throw new RuntimeException("doc.EPCISVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.");
		}

		XNamespace epcisNS = (doc.getEPCISVersion() == EPCISVersion.V2) ? Constants.EPCIS_2_NAMESPACE : Constants.EPCIS_1_NAMESPACE;

		var namespacesReversed = doc.getNamespaces().Reverse();

		// write the events
		JArray jEventList = new JArray();
		JObject jEventBody = new JObject();
		jEventBody["eventList"] = jEventList;
		for (IEvent e : doc.getEvents())
		{
			System.Nullable<JToken> tempVar = OpenTraceabilityJsonLDMapper.ToJson(e, namespacesReversed);
			JObject jEvent = tempVar instanceof JObject ? (JObject)tempVar : null;
			if (jEvent != null)
			{
				EPCISDocumentBaseJsonMapper.PostWriteEventCleanUp(jEvent);
				jEventList.Add(jEvent);
			}
		}

//C# TO JAVA CONVERTER TASK: There is no equivalent to 'await' in Java:
		JObject json = await EPCISDocumentBaseJsonMapper.WriteJsonAsync(doc, epcisNS, "EPCISDocument");

		// write the header
		if (!((doc.getHeader() == null ? null : ((doc.getHeader().getSender() == null ? null : doc.getHeader().getSender().getIdentifier()))) == null || (doc.getHeader() == null ? null : ((doc.getHeader().getSender() == null ? null : doc.getHeader().getSender().getIdentifier()))).isBlank()))
		{
			json["sender"] = doc.getHeader().getSender().getIdentifier();
		}

		if (!((doc.getHeader() == null ? null : ((doc.getHeader().getReceiver() == null ? null : doc.getHeader().getReceiver().getIdentifier()))) == null || (doc.getHeader() == null ? null : ((doc.getHeader().getReceiver() == null ? null : doc.getHeader().getReceiver().getIdentifier()))).isBlank()))
		{
			json["receiver"] = doc.getHeader().getReceiver().getIdentifier();
		}

		if (!((doc.getHeader() == null ? null : ((doc.getHeader().getDocumentIdentification() == null ? null : doc.getHeader().getDocumentIdentification().getInstanceIdentifier()))) == null || (doc.getHeader() == null ? null : ((doc.getHeader().getDocumentIdentification() == null ? null : doc.getHeader().getDocumentIdentification().getInstanceIdentifier()))).isBlank()))
		{
			json["instanceIdentifier"] = doc.getHeader().getDocumentIdentification().getInstanceIdentifier();
		}

		EPCISJsonMasterDataWriter.WriteMasterData(json, doc);

		json["epcisBody"] = jEventBody;

		// conform the JSON-LD to the compacted version with CURIE's that EPCIS 2.0 likes
		EPCISDocumentBaseJsonMapper.ConformEPCISJsonLD(json, doc.getNamespaces());

		// validate the JSON-LD schema
//C# TO JAVA CONVERTER TASK: There is no equivalent to 'await' in Java:
		await EPCISDocumentBaseJsonMapper.CheckSchemaAsync(json);

		return json.toString(Newtonsoft.Json.Formatting.Indented);
	}
}
