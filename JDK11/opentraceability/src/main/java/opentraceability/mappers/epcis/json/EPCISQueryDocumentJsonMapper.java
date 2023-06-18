package opentraceability.mappers.epcis.json;

import opentraceability.interfaces.*;
import opentraceability.models.events.*;
import opentraceability.utility.*;
import opentraceability.*;
import opentraceability.mappers.*;

import org.json.JSONArray;
import org.json.JSONObject;

public class EPCISQueryDocumentJsonMapper implements IEPCISQueryDocumentMapper
{
//C# TO JAVA CONVERTER TASK: There is no equivalent in Java to the 'async' keyword:
//ORIGINAL LINE: public async Task<EPCISQueryDocument> MapAsync(string strValue, bool checkSchema = true)
//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
	public EPCISQueryDocument map(String strValue, boolean checkSchema) throws Exception {
		Pair<EPCISBaseDocument, JSONObject> pair = EPCISDocumentBaseJsonMapper.ReadJSONAsync(strValue, "EPCISQueryDocument", true);
		EPCISQueryDocument doc = (EPCISQueryDocument)pair.getFirst();
		JSONObject json = pair.getSecond();

		if (doc.epcisVersion != EPCISVersion.V2)
		{
			throw new RuntimeException("doc.EPCISVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.");
		}

		// read the query name epcisBody:queryResults:queryName
		doc.QueryName = JSONExtensions.query(json, "epcisBody:queryResults:queryName").toString();

		// read subscription ID, epcisBody:queryResults:subscriptionID
		doc.SubscriptionID = JSONExtensions.query(json, "epcisBody:queryResults:subscriptionID").toString();

		// read the events epcisBody:queryResults:resultsBody:eventList
		JSONArray jEventsList = JSONExtensions.queryForArray(json, "epcisBody:queryResults:resultsBody:eventList");
		if (jEventsList != null)
		{
			for (Object o : jEventsList)
			{
				if (o instanceof JSONObject)
				{
					JSONObject jEvent = (JSONObject)o;
					Class eventType = EPCISDocumentBaseJsonMapper.GetEventTypeFromProfile(jEvent);
					IEvent e = (IEvent)OpenTraceabilityJsonLDMapper.FromJson(jEvent, eventType, doc.namespaces);
					doc.events.add(e);
				}
			}
		}

		return doc;
	}

	public String map(EPCISQueryDocument doc) throws Exception {
		if (doc.epcisVersion != EPCISVersion.V2)
		{
			throw new RuntimeException("doc.EPCISVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.");
		}

		String epcisNS = (doc.epcisVersion == EPCISVersion.V2) ? Constants.EPCISQUERY_2_NAMESPACE : Constants.EPCISQUERY_1_NAMESPACE;

		var namespacesReversed = DictionaryExtensions.reverse(doc.namespaces);

		// write the events
		JSONArray jEventsList = new JSONArray();
		for (IEvent e : doc.events)
		{
			Object o = OpenTraceabilityJsonLDMapper.ToJson(e, namespacesReversed);
			JSONObject jEvent = o instanceof JSONObject ? (JSONObject)o : null;
			if (jEvent != null)
			{
				EPCISDocumentBaseJsonMapper.PostWriteEventCleanUp(jEvent);
				jEventsList.put(jEvent);
			}
		}

//C# TO JAVA CONVERTER TASK: There is no equivalent to 'await' in Java:
		JSONObject json = EPCISDocumentBaseJsonMapper.WriteJson(doc, epcisNS, "EPCISQueryDocument");

		JSONObject jEPCISBody = new JSONObject();
		JSONObject jQueryResults = new JSONObject();
		JSONObject jResultsBody = new JSONObject();

		jQueryResults.put("queryName", doc.QueryName);
		jQueryResults.put("subscriptionID", doc.SubscriptionID);

		jResultsBody.put("eventList", jEventsList);
		jQueryResults.put("resultsBody", jResultsBody);
		jEPCISBody.put("queryResults", jQueryResults);
		json.put("epcisBody", jEPCISBody);

		// conform the JSON-LD to the compacted version with CURIE's that EPCIS 2.0 likes
		EPCISDocumentBaseJsonMapper.conformEPCISJsonLD(json, doc.namespaces);

		EPCISDocumentBaseJsonMapper.checkSchema(json);

		return json.toString();
	}
}
