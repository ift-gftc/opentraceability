package opentraceability.mappers.epcis.json;

import Newtonsoft.Json.Linq.*;
import opentraceability.interfaces.*;
import opentraceability.models.events.*;
import opentraceability.utility.*;
import opentraceability.*;
import opentraceability.mappers.*;
import opentraceability.mappers.epcis.*;

public class EPCISQueryDocumentJsonMapper implements IEPCISQueryDocumentMapper
{

	public final EPCISQueryDocument Map(String strValue)
	{
		return Map(strValue, true);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public EPCISQueryDocument Map(string strValue, bool checkSchema = true)
	public final EPCISQueryDocument Map(String strValue, boolean checkSchema)
	{
		return MapAsync(strValue, checkSchema).GetAwaiter().GetResult();
	}


	public final Task<EPCISQueryDocument> MapAsync(String strValue)
	{
		return MapAsync(strValue, true);
	}

//C# TO JAVA CONVERTER TASK: There is no equivalent in Java to the 'async' keyword:
//ORIGINAL LINE: public async Task<EPCISQueryDocument> MapAsync(string strValue, bool checkSchema = true)
//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
	public final Task<EPCISQueryDocument> MapAsync(String strValue, boolean checkSchema)
	{
		try
		{
//C# TO JAVA CONVERTER TASK: Java has no equivalent to C# deconstruction declarations:
			(EPCISQueryDocument doc, JObject json) = await EPCISDocumentBaseJsonMapper.<EPCISQueryDocument>ReadJSONAsync(strValue, "EPCISQueryDocument", checkSchema);

			if (doc.EPCISVersion != EPCISVersion.V2)
			{
				throw new RuntimeException("doc.EPCISVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.");
			}

			// read the query name
			doc.QueryName = json["epcisBody"] == null ? null : (((json["epcisBody"]["queryResults"] == null ? null : ((json["epcisBody"]["queryResults"]["queryName"] == null ? null : json["epcisBody"]["queryResults"]["queryName"].toString())))) != null ? ((json["epcisBody"]["queryResults"] == null ? null : ((json["epcisBody"]["queryResults"]["queryName"] == null ? null : json["epcisBody"]["queryResults"]["queryName"].toString())))) : "");
			doc.SubscriptionID = json["epcisBody"] == null ? null : (((json["epcisBody"]["queryResults"] == null ? null : ((json["epcisBody"]["queryResults"]["subscriptionID"] == null ? null : json["epcisBody"]["queryResults"]["subscriptionID"].toString())))) != null ? ((json["epcisBody"]["queryResults"] == null ? null : ((json["epcisBody"]["queryResults"]["subscriptionID"] == null ? null : json["epcisBody"]["queryResults"]["subscriptionID"].toString())))) : "");

			// read the events
			Object tempVar = ((json["epcisBody"]["queryResults"] == null ? null : ((json["epcisBody"]["queryResults"]["resultsBody"] == null ? null : json["epcisBody"]["queryResults"]["resultsBody"]["eventList"]))));
			JArray jEventsList = json["epcisBody"] == null ? null : tempVar instanceof JArray ? (JArray)tempVar : null;
			if (jEventsList != null)
			{
				for (JObject jEvent : jEventsList)
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
			RuntimeException exception = new RuntimeException("Failed to parse the EPCIS document from the XML. xml=" + strValue, Ex);
			OTLogger.Error(exception);
			throw Ex;
		}
	}

	public final String Map(EPCISQueryDocument doc)
	{
		return MapAsync(doc).GetAwaiter().GetResult();
	}

//C# TO JAVA CONVERTER TASK: There is no equivalent in Java to the 'async' keyword:
//ORIGINAL LINE: public async Task<string> MapAsync(EPCISQueryDocument doc)
	public final Task<String> MapAsync(EPCISQueryDocument doc)
	{
		if (doc.getEPCISVersion() != EPCISVersion.V2)
		{
			throw new RuntimeException("doc.EPCISVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.");
		}

		XNamespace epcisNS = (doc.getEPCISVersion() == EPCISVersion.V2) ? Constants.EPCISQUERY_2_NAMESPACE : Constants.EPCISQUERY_1_NAMESPACE;

		var namespacesReversed = doc.getNamespaces().Reverse();

		// write the events
		JArray jEventsList = new JArray();
		for (IEvent e : doc.getEvents())
		{
			System.Nullable<JToken> tempVar = OpenTraceabilityJsonLDMapper.ToJson(e, namespacesReversed);
			JObject jEvent = tempVar instanceof JObject ? (JObject)tempVar : null;
			if (jEvent != null)
			{
				EPCISDocumentBaseJsonMapper.PostWriteEventCleanUp(jEvent);
				jEventsList.Add(jEvent);
			}
		}

//C# TO JAVA CONVERTER TASK: There is no equivalent to 'await' in Java:
		JObject json = await EPCISDocumentBaseJsonMapper.WriteJsonAsync(doc, epcisNS, "EPCISQueryDocument");

		JObject jEPCISBody = new JObject();
		JObject jQueryResults = new JObject();
		JObject jResultsBody = new JObject();

		jQueryResults["queryName"] = doc.getQueryName();
		jQueryResults["subscriptionID"] = doc.getSubscriptionID();

		jResultsBody["eventList"] = jEventsList;
		jQueryResults["resultsBody"] = jResultsBody;
		jEPCISBody["queryResults"] = jQueryResults;
		json["epcisBody"] = jEPCISBody;

		// conform the JSON-LD to the compacted version with CURIE's that EPCIS 2.0 likes
		EPCISDocumentBaseJsonMapper.ConformEPCISJsonLD(json, doc.getNamespaces());

		// validate the JSON-LD schema
//C# TO JAVA CONVERTER TASK: There is no equivalent to 'await' in Java:
		await EPCISDocumentBaseJsonMapper.CheckSchemaAsync(json);

		return json.toString();
	}
}
