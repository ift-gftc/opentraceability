package opentraceability.mappers.epcis.json;

import opentraceability.interfaces.*;
import opentraceability.models.common.*;
import opentraceability.models.events.*;
import opentraceability.utility.*;
import opentraceability.*;
import org.json.JSONArray;
import org.json.JSONObject;
import tangible.StringHelper;

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import javax.swing.text.Document;

public final class EPCISDocumentBaseJsonMapper
{
//C# TO JAVA CONVERTER TASK: Methods returning tuples are not converted by C# to Java Converter:
	public static Pair<EPCISBaseDocument, JSONObject> ReadJSONAsync(String strValue, String expectedType, Boolean checkSchema) throws Exception {
			// validate the JSON...
			if (checkSchema)
			{
				checkSchema(new JSONObject(strValue));
			}

			// normalize the json-ld
			strValue = normalizeEPCISJsonLD(strValue);

			JSONObject json = new JSONObject(strValue);

			if (!expectedType.equals("EPCISDocument") && !expectedType.equals("EPCISQueryDocument"))
			{
				throw new Exception("expectedType has to be EPCISDocument or EPCISQueryDocument. expectedType=" + expectedType);
			}

			if (!json.has("type") || !json.getString("type").equals(expectedType))
			{
				throw new Exception("Failed to parse json from string. Expected type=" + expectedType + ", actual type=" + (json.has("type") ? json.getString("type") : ""));
			}

			// read all of the attributes
			EPCISBaseDocument document = null;
			if (expectedType.equals("EPCISDocument"))
			{
				document = new EPCISDocument();
			}
			else
			{
				document = new EPCISQueryDocument();
			}

			document.attributes.put("schemaVersion", (json.has("schemaVersion") ? json.getString("schemaVersion") : ""));
			document.epcisVersion = EPCISVersion.V2;

			// read the creation date from json.get("creationDate") and try and parse as ISO DATE TIME
			String creationDateAttributeStr = (json.has("creationDate") ? json.getString("creationDate") : "");
			if (!StringHelper.isNullOrEmpty(creationDateAttributeStr))
			{
				document.creationDate = OffsetDateTime.parse(creationDateAttributeStr, DateTimeFormatter.ISO_DATE_TIME);
			}

			// read the content...
			document.attributes = new HashMap<>();

			// we are going to break down the content into either namespaces, or links to contexts...
			JSONArray jContextArray = json.has("@context") && json.get("@context") instanceof JSONArray ? json.getJSONArray("@context") : null;
			if (jContextArray != null)
			{
				for (int i = 0; i < jContextArray.length(); i++)
				{
					Object item = jContextArray.get(i);
					if (item instanceof JSONObject)
					{
						JSONObject jobj = (JSONObject)item;
						var ns = JsonContextHelper.scrapeNamespaces(jobj);
						for (var n: ns.keySet())
						{
							if (!document.namespaces.containsKey(n))
							{
								document.namespaces.put(n, ns.get(n));
							}
						}

						// add it to the contexts..
						document.contexts.add(jobj.toString());
					}
					else
					{
						String val = item.toString();
						if (!tangible.StringHelper.isNullOrEmpty(val))
						{
							// if this is a URL, then resolve it and grab the namespaces...
							JSONObject jcontext = JsonContextHelper.getJsonLDContext(val);
							var ns = JsonContextHelper.scrapeNamespaces(jcontext);
							for (var n: ns.keySet())
							{
								if (!document.namespaces.containsKey(n))
								{
									document.namespaces.put(n, ns.get(n));
								}
							}

							// add it to the contexts..
							document.contexts.add(val);
						}
					}
				}
			}
			else
				throw new Exception("the @context on the root of the JSON-LD EPCIS file was not an array. we are currently expecting this to be an array.");

			if (json.has("id"))
			{
				document.attributes.put("id", json.get("id").toString());
			}

			// read header information
			document.header = new StandardBusinessDocumentHeader();

			document.header.Sender = new SBDHOrganization();
			document.header.Sender.Identifier = json.has("sender") ? json.getString("sender") : "";

			document.header.Receiver = new SBDHOrganization();
			document.header.Receiver.Identifier = json.has("receiver") ? json.getString("receiver") : "";

			document.header.DocumentIdentification = new SBDHDocumentIdentification();
			document.header.DocumentIdentification.InstanceIdentifier = json.has("instanceIdentifier") ? json.getString("instanceIdentifier") : "";

			return new Pair<>(document, json);
		}


	public static JSONObject WriteJson(EPCISBaseDocument doc, String epcisNS, String docType) throws Exception {
		if (!doc.epcisVersion.equals(EPCISVersion.V2))
		{
			throw new RuntimeException("doc.EPCISVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.");
		}

		// create a new xdocument with all of the namespaces...
		JSONObject json = new JSONObject();

		// write the context
		JSONArray jContext = new JSONArray();

		if (!doc.contexts.contains("https://ref.gs1.org/standards/epcis/epcis-context.jsonld") 
				&& !doc.contexts.contains("https://gs1.github.io/EPCIS/epcis-context.jsonld"))
		{
			doc.contexts.add("https://ref.gs1.org/standards/epcis/epcis-context.jsonld");
		}

		ArrayList<String> namespacesAlreadyWritten = new ArrayList<String>();
		for (String context : doc.contexts)
		{
//C# TO JAVA CONVERTER TASK: The following method call contained an unresolved 'out' keyword - these cannot be converted using the 'OutObject' helper class unless the method is within the code being modified:
			if (StringExtensions.isURI(context))
			{
//C# TO JAVA CONVERTER TASK: There is no equivalent to 'await' in Java:
				JSONObject jc = JsonContextHelper.getJsonLDContext(context);
				var ns = JsonContextHelper.scrapeNamespaces(jc);
				for (var n : ns.entrySet())
				{
					doc.namespaces.remove(n.getKey());
				}

				jContext.put(context);
			}
			else
			{
				JSONObject jobj = new JSONObject(context);
				if (JsonContextHelper.isComplexContext(jobj))
				{
					var ns = JsonContextHelper.scrapeNamespaces(jobj);
					for (var kvp : ns.entrySet())
					{
						namespacesAlreadyWritten.add(kvp.getValue());
					}

					jContext.put(jobj);
				}
			}
		}

		for (var ns : doc.namespaces.entrySet())
		{
			if (!namespacesAlreadyWritten.contains(ns.getValue()))
			{
				JSONObject j = new JSONObject();
				j.put(ns.getKey(), ns.getValue());
				jContext.put(j);
			}
		}

		json.put("@context", jContext);

		// write the type
		json.put("type", docType);

		// set the creation date
		if (doc.creationDate != null)
		{
			json.put("creationDate", doc.creationDate.format(DateTimeFormatter.ISO_DATE_TIME));
		}

		json.put("schemaVersion", "2.0");

		// extra attributes
		if (doc.attributes.containsKey("id"))
		{
			json.put("id", doc.attributes.get("id"));
		}

		return json;
	}

	/** 
	 This performs a final cleanup on the JSON-LD document.
	 
	 @param json
	*/
	public static void PostWriteEventCleanUp(JSONObject json)
	{
		// when converting from XML to JSON, the XML allows an empty readPoint, but the JSON does not.
		if (json.has("readPoint") && json.get("readPoint") instanceof JSONObject)
		{
			JSONObject jReadPoint = (JSONObject)json.get("readPoint");
			if (jReadPoint.has("id") && jReadPoint.get("id") instanceof String && StringHelper.isNullOrEmpty(jReadPoint.getString("id")))
			{
				json.remove("readPoint");
			}
		}
	}

	public static Class GetEventTypeFromProfile(JSONObject jEvent)
	{
		String action = (jEvent.has("action") ? jEvent.getString("action") : "");
		String bizStep = (jEvent.has("bizStep") ? jEvent.getString("bizStep") : "");
		String eventType = (jEvent.has("type") ? jEvent.getString("type") : "");

		if (bizStep == null)
		{
			bizStep = "";
		}

		String finalBizStep = bizStep.toLowerCase();
		var profiles = Setup.Profiles.stream().filter(p -> p.EventType.toString().toLowerCase().equals(eventType.toLowerCase())
				&& (p.Action == null || p.Action.toString() == action)
				&& (StringHelper.isNullOrEmpty(p.BusinessStep) || p.BusinessStep.toLowerCase().equals(finalBizStep)));

		List<OpenTraceabilityEventProfile> finalProfiles = profiles.collect(Collectors.toList());

		if (finalProfiles.size() == 0)
		{
			throw new RuntimeException("Failed to create event from profile. Type=" + eventType + " and BizStep=" + bizStep + " and Action=" + action);
		}
		else
		{
			for (var profile : finalProfiles.stream().filter(p -> p.KDEProfiles != null).collect(Collectors.toList()))
			{
				if (profile.KDEProfiles != null)
				{
					for (var kdeProfile : profile.KDEProfiles)
					{
						if (jEvent.query(kdeProfile.JPath) == null)
						{
							finalProfiles.remove(profile);
						}
					}
				}
			}

			if (finalProfiles.size() == 0)
			{
				throw new RuntimeException("Failed to create event from profile. Type=" + eventType + " and BizStep=" + bizStep + " and Action=" + action);
			}

			return ((OpenTraceabilityEventProfile)finalProfiles.toArray()[0]).EventClassType;
		}
	}

	public static void checkSchema(JSONObject json) throws URISyntaxException, IOException, InterruptedException, OpenTraceabilitySchemaException {
		String jsonStr = json.toString();
		Pair<Boolean, List<String>> results = JsonSchemaChecker.isValid(jsonStr, "https://ref.gs1.org/standards/epcis/epcis-json-schema.json");
		if (!results.getSecond().isEmpty())
		{
			throw new OpenTraceabilitySchemaException("Failed to validate JSON schema with errors:\n" + tangible.StringHelper.join("\n", results.getSecond().toArray(new String[0])) + "\n\n and json " + json);
		}
	}

	public static String GetEventType(IEvent e)
	{
		if (e.eventType == EventType.ObjectEvent)
		{
			return "ObjectEvent";
		}
		else if (e.eventType == EventType.TransformationEvent)
		{
			return "TransformationEvent";
		}
		else if (e.eventType == EventType.TransactionEvent)
		{
			return "TransactionEvent";
		}
		else if (e.eventType == EventType.AggregationEvent)
		{
			return "AggregationEvent";
		}
		else if (e.eventType == EventType.AssociationEvent)
		{
			return "AssociationEvent";
		}
		else
		{
			throw new RuntimeException("Failed to determine the event type. Event C# type is " + e.getClass().getName());
		}
	}

	public static void conformEPCISJsonLD(JSONObject json, HashMap<String, String> namespaces) throws Exception {
		CompressVocab(json);
	}

	private static Object CompressVocab(Object json) throws Exception {
		if (json instanceof JSONObject)
		{
			JSONObject jobj = (JSONObject)json;
			for (var jprop : jobj.keySet())
			{
				if (jobj.has(jprop))
				{
					Object jvalue = jobj.get(jprop);
					if (jvalue instanceof JSONObject)
					{
						jobj.put(jprop, CompressVocab(jvalue));
					}
					else if (jvalue instanceof JSONArray)
					{
						JSONArray ja = (JSONArray)jvalue;
						for (int i = 0; i < ja.length(); i++)
						{
							Object jt = ja.get(i);
							ja.put(i, CompressVocab(jt));
						}
					}
					else if (jvalue != null)
					{
						jobj.put(jprop, CompressVocab(jvalue));
					}
				}
			}
			return jobj;
		}
		else
		{
			String val = json.toString();
			if (val != null)
			{
				if (val.startsWith("urn:epcglobal:cbv:btt:") || val.startsWith("urn:epcglobal:cbv:bizstep:") || val.startsWith("urn:epcglobal:cbv:sdt:") || val.startsWith("urn:epcglobal:cbv:disp:"))
				{
					val = StringExtensions.Last(val.split(java.util.regex.Pattern.quote(":"), -1));
					return val;
				}
				else if (val.startsWith("https://ref.gs1.org/cbv"))
				{
					val = StringExtensions.Last(val.split(java.util.regex.Pattern.quote("-"), -1));
					return val;
				}
				else if (val.startsWith("https://gs1.org/voc/"))
				{
					val = StringExtensions.Last(val.split(java.util.regex.Pattern.quote("/"), -1));
					return val;
				}
			}
			return json;
		}
	}

	/** 
	 This will take an EPCIS Query Document or an EPCIS Document in the JSON-LD format
	 and it will normalize the document so that all of the CURIEs are expanded into full
	 URIs and that the JSON-LD is compacted.
	 https://ref.gs1.org/standards/epcis/epcis-context.jsonld
	*/
	public static String normalizeEPCISJsonLD(String jEPCISStr) throws Exception {
		JSONObject json = new JSONObject(jEPCISStr);

		JSONObject jEPCISContext = JsonContextHelper.getJsonLDContext("https://ref.gs1.org/standards/epcis/epcis-context.jsonld");
		Map<String, String> namespaces = JsonContextHelper.scrapeNamespaces(jEPCISContext);

		JSONArray jEventList = JSONExtensions.queryForArray(json,"epcisBody.eventList");
		if (jEventList == null)
		{
			jEventList = JSONExtensions.queryForArray(json,"epcisBody.queryResults.resultsBody.eventList");
		}

		if (jEventList != null)
		{
			for (Object jEvent : jEventList)
			{
				if (jEvent instanceof JSONObject)
				{
					JSONObject jEventObj = (JSONObject) jEvent;
					JsonContextHelper.expandVocab(jEvent, jEPCISContext, namespaces, null);
				}
			}
		}

		return json.toString();
	}
}
