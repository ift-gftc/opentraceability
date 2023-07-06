package opentraceability.mappers.epcis.json;

import opentraceability.interfaces.*;
import opentraceability.mappers.epcis.xml.*;
import opentraceability.models.events.*;
import opentraceability.utility.*;
import opentraceability.*;
import opentraceability.mappers.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import java.net.URISyntaxException;

public class EPCISDocumentJsonMapper implements IEPCISDocumentMapper
{
	public final EPCISDocument map(String strValue, Boolean checkSchema) throws Exception
	{
		Pair<EPCISBaseDocument, JSONObject> pair = EPCISDocumentBaseJsonMapper.ReadJSONAsync(strValue, "EPCISDocument", checkSchema);
		EPCISDocument doc = (EPCISDocument)pair.getFirst();
		JSONObject json = pair.getSecond();

		if (doc.epcisVersion != EPCISVersion.V2)
		{
			throw new RuntimeException("doc.EPCISVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.");
		}

		// read the master data
		JSONObject jMasterData = JSONExtensions.queryForObject(json, "epcisHeader.epcisMasterData");
		if (jMasterData != null)
		{
			EPCISJsonMasterDataReader.ReadMasterData(doc, jMasterData);
		}

		// read the events epcisBody:eventList
		JSONArray jEventList = JSONExtensions.queryForArray(json, "epcisBody.eventList");
		if (jEventList != null)
		{
			for (var item : jEventList)
			{
				if (item instanceof JSONObject)
				{
					JSONObject jEvent = (org.json.JSONObject) item;
					Class eventType = EPCISDocumentBaseJsonMapper.GetEventTypeFromProfile(jEvent);
					IEvent e = (IEvent)OpenTraceabilityJsonLDMapper.FromJson(jEvent, eventType, doc.namespaces);
					doc.events.add(e);
				}
			}
		}

		return doc;
	}

//C# TO JAVA CONVERTER TASK: There is no equivalent in Java to the 'async' keyword:
//ORIGINAL LINE: public async Task<string> MapAsync(EPCISDocument doc)
	public String map(EPCISDocument doc, Boolean checkSchema) throws Exception
	{
		if (doc.epcisVersion != EPCISVersion.V2)
		{
			throw new RuntimeException("doc.EPCISVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.");
		}

		String epcisNS = (doc.epcisVersion == EPCISVersion.V2) ? Constants.EPCIS_2_NAMESPACE : Constants.EPCIS_1_NAMESPACE;

		var namespacesReversed = DictionaryExtensions.reverse(doc.namespaces);

		// write the events
		JSONArray jEventList = new JSONArray();
		JSONObject jEventBody = new JSONObject();
		jEventBody.put("eventList", jEventList);

		for (IEvent e : doc.events)
		{
			Object tempVar = OpenTraceabilityJsonLDMapper.ToJson(e, namespacesReversed, false, false);
			JSONObject jEvent = tempVar instanceof JSONObject ? (JSONObject)tempVar : null;
			if (jEvent != null)
			{
				EPCISDocumentBaseJsonMapper.PostWriteEventCleanUp(jEvent);
				jEventList.put(jEvent);
			}
		}

		JSONObject json = EPCISDocumentBaseJsonMapper.WriteJson(doc, epcisNS, "EPCISDocument");

		json.put("sender", doc.header.Sender.Identifier);
		json.put("receiver", doc.header.Receiver.Identifier);
		json.put("instanceIdentifier", doc.header.DocumentIdentification.InstanceIdentifier);

		EPCISJsonMasterDataWriter.WriteMasterData(json, doc);

		json.put("epcisBody", jEventBody);

		// conform the JSON-LD to the compacted version with CURIE's that EPCIS 2.0 likes
		EPCISDocumentBaseJsonMapper.conformEPCISJsonLD(json, doc.namespaces);

		// validate the JSON-LD schema
		if (checkSchema) {
			EPCISDocumentBaseJsonMapper.checkSchema(json);
		}

		return json.toString();
	}
}
