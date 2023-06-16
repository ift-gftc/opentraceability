package opentraceability.mappers.epcis.json;

import Newtonsoft.Json.*;
import Newtonsoft.Json.Linq.*;
import opentraceability.interfaces.*;
import opentraceability.models.events.*;
import opentraceability.utility.*;
import opentraceability.*;
import opentraceability.mappers.*;
import opentraceability.mappers.epcis.*;
import java.util.*;

public final class EPCISDocumentBaseJsonMapper
{
//C# TO JAVA CONVERTER TASK: Methods returning tuples are not converted by C# to Java Converter:
//	public static async Task<(T, JObject)> ReadJSONAsync<T>(string strValue, string expectedType, bool checkSchema = true) where T : EPCISBaseDocument, new()
//		{
//			// validate the JSON...
//			if (checkSchema)
//			{
//				await CheckSchemaAsync(JObject.Parse(strValue));
//			}
//
//			// normalize the json-ld
//			strValue = await NormalizeEPCISJsonLDAsync(strValue);
//
//			// convert into XDocument
//			var settings = new JsonSerializerSettings { DateParseHandling = DateParseHandling.None };
//			JObject json = JsonConvert.DeserializeObject<JObject>(strValue, settings) ?? throw new Exception("Failed to parse json from string. " + strValue);
//
//			if ((json["type"] == null ? null : json["type"].ToString()) != expectedType)
//			{
//				throw new Exception("Failed to parse json from string. Expected type=" + expectedType + ", actual type=" + (json["type"] == null ? null : json["type"].ToString()) ?? string.Empty);
//			}
//
//			// read all of the attributes
//			T document = Activator.CreateInstance<T>();
//
//			document.Attributes.Add("schemaVersion", (json["schemaVersion"] == null ? null : json["schemaVersion"].ToString()) ?? string.Empty);
//			document.EPCISVersion = EPCISVersion.V2;
//
//			// read the creation date
//			System.Nullable<string> creationDateAttributeStr = json["creationDate"] == null ? null : json["creationDate"].ToString();
//			if (!string.IsNullOrWhiteSpace(creationDateAttributeStr))
//			{
//				document.CreationDate = creationDateAttributeStr.TryConvertToDateTimeOffset();
//			}
//
//			// read the content...
//			document.Attributes = new Dictionary<string, string>();
//
//			// we are going to break down the content into either namespaces, or links to contexts...
//			System.Nullable<JArray> jContextArray = json["@context"] as JArray;
//			if (jContextArray != null)
//			{
//				foreach (JToken jt in jContextArray)
//				{
//					// go through each item in the array...
//					if (jt is JObject)
//					{
//						// grab all namespaces from the jobject
//						JObject jobj = (JObject)jt;
//						var ns = JsonContextHelper.ScrapeNamespaces(jobj);
//						foreach (var n in ns)
//						{
//							if (!document.Namespaces.ContainsKey(n.Key))
//							{
//								document.Namespaces.Add(n.Key, n.Value);
//							}
//						}
//
//						// add it to the contexts..
//						document.Contexts.Add(jobj.ToString());
//					}
//					else
//					{
//						System.Nullable<string> val = jt.ToString();
//
//						if (!string.IsNullOrWhiteSpace(val))
//						{
//							// if this is a URL, then resolve it and grab the namespaces...
//							JObject jcontext = await JsonContextHelper.GetJsonLDContextAsync(val);
//							var ns = JsonContextHelper.ScrapeNamespaces(jcontext);
//							foreach (var n in ns)
//							{
//								if (!document.Namespaces.ContainsKey(n.Key))
//								{
//									document.Namespaces.Add(n.Key, n.Value);
//								}
//							}
//
//							document.Contexts.Add(val);
//						}
//					}
//				}
//			}
//			else
//				throw new Exception("the @context on the root of the JSON-LD EPCIS file was not an array. we are currently expecting this to be an array.");
//
//			if (json["id"] != null)
//			{
//				document.Attributes.Add("id", json.Value<string>("id") ?? string.Empty);
//			}
//
//			// read header information
//			document.Header = new Models.Common.StandardBusinessDocumentHeader();
//
//			document.Header.Sender = new Models.Common.SBDHOrganization();
//			document.Header.Sender.Identifier = json["sender"] == null ? null : json["sender"].ToString() ?? string.Empty;
//
//			document.Header.Receiver = new Models.Common.SBDHOrganization();
//			document.Header.Receiver.Identifier = json["receiver"] == null ? null : json["receiver"].ToString() ?? string.Empty;
//
//			document.Header.DocumentIdentification = new Models.Common.SBDHDocumentIdentification();
//			document.Header.DocumentIdentification.InstanceIdentifier = json["instanceIdentifier"] == null ? null : json["instanceIdentifier"].ToString() ?? string.Empty;
//
//			return (document, json);
//		}

//C# TO JAVA CONVERTER TASK: There is no equivalent in Java to the 'async' keyword:
//ORIGINAL LINE: public static async Task<JObject> WriteJsonAsync(EPCISBaseDocument doc, XNamespace epcisNS, string docType)
	public static Task<JObject> WriteJsonAsync(EPCISBaseDocument doc, XNamespace epcisNS, String docType)
	{
		if (doc.getEPCISVersion() != EPCISVersion.V2)
		{
			throw new RuntimeException("doc.EPCISVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.");
		}

		// create a new xdocument with all of the namespaces...
		JObject json = new JObject();

		// write the context
		JArray jContext = new JArray();

		if (!doc.getContexts().contains("https://ref.gs1.org/standards/epcis/epcis-context.jsonld") && !doc.getContexts().contains("https://gs1.github.io/EPCIS/epcis-context.jsonld"))
		{
			doc.getContexts().add("https://ref.gs1.org/standards/epcis/epcis-context.jsonld");
		}

		ArrayList<String> namespacesAlreadyWritten = new ArrayList<String>();
		for (String context : doc.getContexts())
		{
			var uri;
//C# TO JAVA CONVERTER TASK: The following method call contained an unresolved 'out' keyword - these cannot be converted using the 'OutObject' helper class unless the method is within the code being modified:
			if (Uri.TryCreate(context, UriKind.Absolute, out uri))
			{
//C# TO JAVA CONVERTER TASK: There is no equivalent to 'await' in Java:
				JObject jc = await JsonContextHelper.GetJsonLDContextAsync(context);
				var ns = JsonContextHelper.ScrapeNamespaces(jc);
				for (var n : ns.entrySet())
				{
					if (doc.getNamespaces().containsKey(n.getKey()))
					{
						doc.getNamespaces().remove(n.getKey());
					}
				}

				jContext.Add(JToken.FromObject(context));
			}
			else
			{
				JObject jobj = JObject.Parse(context);
				if (JsonContextHelper.IsComplexContext(jobj))
				{
					var ns = JsonContextHelper.ScrapeNamespaces(jobj);
					for (var kvp : ns.entrySet())
					{
						namespacesAlreadyWritten.add(kvp.getValue());
					}

					jContext.Add(jobj);
				}
			}
		}

		for (var ns : doc.getNamespaces().entrySet())
		{
			if (!namespacesAlreadyWritten.contains(ns.getValue()))
			{
				JObject j = new JObject();
				j[ns.getKey()] = ns.getValue();
				jContext.Add(j);
			}
		}

		json["@context"] = jContext;

		// write the type
		json["type"] = docType;

		// set the creation date
		if (doc.getCreationDate() != null)
		{
			json["creationDate"] = doc.getCreationDate().getValue().toString("O");
		}

		json["schemaVersion"] = "2.0";

		// extra attributes
		if (doc.getAttributes().containsKey("id"))
		{
			json["id"] = doc.getAttributes()["id"];
		}

		return json;
	}

	/** 
	 This performs a final cleanup on the JSON-LD document.
	 
	 @param json
	*/
	public static void PostWriteEventCleanUp(JObject json)
	{
		// when converting from XML to JSON, the XML allows an empty readPoint, but the JSON does not.
		if (json["readPoint"] instanceof JObject && (json["readPoint"] == null ? null : json["readPoint"]["id"]) == null)
		{
			json.Remove("readPoint");
		}
	}

	public static java.lang.Class GetEventTypeFromProfile(JObject jEvent)
	{
		var action;
//C# TO JAVA CONVERTER TASK: The following method call contained an unresolved 'out' keyword - these cannot be converted using the 'OutObject' helper class unless the method is within the code being modified:
		Enum.<EventAction>TryParse((jEvent["action"] == null ? null : jEvent["action"].toString()), out action);
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: string? bizStep = jEvent["bizStep"] == null ? null : jEvent["bizStep"].ToString();
		String bizStep = jEvent["bizStep"] == null ? null : jEvent["bizStep"].toString();
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: string eventType = jEvent["type"] == null ? null : jEvent["type"].ToString() ?? throw new Exception("type property not set on event " + jEvent.ToString());
		String eventType = jEvent["type"] == null ? null : ((jEvent["type"].toString()) != null ? jEvent["type"].toString() : throw new RuntimeException("type property not set on event " + jEvent.toString()));

		var profiles = Setup.Profiles.Where(p -> Objects.equals(p.EventType.toString(), eventType) && (p.Action == null || p.Action == action) && (p.BusinessStep == null || Objects.equals(p.BusinessStep.toLowerCase(), (bizStep == null ? null : bizStep.toLowerCase())))).OrderByDescending(p -> p.SpecificityScore).ToList();
		if (profiles.size() == 0)
		{
			throw new RuntimeException("Failed to create event from profile. Type=" + eventType + " and BizStep=" + bizStep + " and Action=" + action);
		}
		else
		{
			for (var profile : profiles.stream().filter(p -> p.KDEProfiles != null).collect(Collectors.toList()))
			{
				if (profile.KDEProfiles != null)
				{
					for (var kdeProfile : profile.KDEProfiles)
					{
						if (jEvent.QueryJPath(kdeProfile.JPath) == null)
						{
							profiles.remove(profile);
						}
					}
				}
			}

			if (profiles.size() == 0)
			{
				throw new RuntimeException("Failed to create event from profile. Type=" + eventType + " and BizStep=" + bizStep + " and Action=" + action);
			}

			return profiles.get(0).EventClassType;
		}
	}

//C# TO JAVA CONVERTER TASK: There is no equivalent in Java to the 'async' keyword:
//ORIGINAL LINE: internal static async Task CheckSchemaAsync(JObject json)
	public static Task CheckSchemaAsync(JObject json)
	{
		String jsonStr = json.toString();
//C# TO JAVA CONVERTER TASK: There is no equivalent to 'await' in Java:
		ArrayList<String> errors = await JsonSchemaChecker.IsValidAsync(jsonStr, "https://ref.gs1.org/standards/epcis/epcis-json-schema.json");
		if (!errors.isEmpty())
		{
			throw new OpenTraceabilitySchemaException("Failed to validate JSON schema with errors:\n" + tangible.StringHelper.join('\n', errors) + "\n\n and json " + json.toString(Formatting.Indented));
		}
	}

	public static String GetEventType(IEvent e)
	{
		if (e.getEventType() == EventType.ObjectEvent)
		{
			return "ObjectEvent";
		}
		else if (e.getEventType() == EventType.TransformationEvent)
		{
			return "TransformationEvent";
		}
		else if (e.getEventType() == EventType.TransactionEvent)
		{
			return "TransactionEvent";
		}
		else if (e.getEventType() == EventType.AggregationEvent)
		{
			return "AggregationEvent";
		}
		else if (e.getEventType() == EventType.AssociationEvent)
		{
			return "AssociationEvent";
		}
		else
		{
			throw new RuntimeException("Failed to determine the event type. Event C# type is " + e.getClass().getName());
		}
	}

	/** 
	 This will take an EPCIS JSON-LD document and make sure that everything is set for
	 it to pass the JSON schema for EPCIS 2.0. This includes expanding the CURIEs, etc.
	 
	 @param jEPCISStr
	 @return 
	*/
	public static void ConformEPCISJsonLD(JObject json, HashMap<String, String> namespaces)
	{
		CompressVocab(json);
		//JObject jEPCISContext = JsonContextHelper.GetJsonLDContext("https://ref.gs1.org/standards/epcis/epcis-context.jsonld");

		//JArray? jEventList = json["epcisBody"]?["eventList"] as JArray;
		//if (jEventList == null)
		//{
		//    jEventList = json["epcisBody"]?["queryResults"]?["resultsBody"]?["eventList"] as JArray;
		//}
		//if (jEventList != null)
		//{
		//    foreach (JObject jEvent in jEventList)
		//    {
		//        JsonContextHelper.CompressVocab(jEvent, jEPCISContext, namespaces);
		//    }
		//}
	}

	private static JToken CompressVocab(JToken json)
	{
		if (json instanceof JObject)
		{
			JObject jobj = (JObject)json;
			for (var jprop : jobj.Properties())
			{
				JToken jvalue = jobj[jprop.Name];
				if (jvalue instanceof JObject)
				{
					json[jprop.Name] = CompressVocab((JObject)jvalue);
				}
				else if (jvalue instanceof JArray)
				{
					JArray ja = (JArray)jvalue;
					for (int i = 0; i < ja.Count; i++)
					{
						JToken jt = ja[i];
						ja[i] = CompressVocab(jt);
					}
				}
				else if (jvalue != null)
				{
					json[jprop.Name] = CompressVocab(jvalue);
				}
			}
			return jobj;
		}
		else
		{
//C# TO JAVA CONVERTER WARNING: Nullable reference types have no equivalent in Java:
//ORIGINAL LINE: string? val = json.ToString();
			String val = json.toString();
			if (val != null)
			{
				if (val.startsWith("urn:epcglobal:cbv:btt:") || val.startsWith("urn:epcglobal:cbv:bizstep:") || val.startsWith("urn:epcglobal:cbv:sdt:") || val.startsWith("urn:epcglobal:cbv:disp:"))
				{
					val = val.split(java.util.regex.Pattern.quote(":"), -1).Last();
					return JToken.FromObject(val);
				}
				else if (val.startsWith("https://ref.gs1.org/cbv"))
				{
					val = val.split(java.util.regex.Pattern.quote("-"), -1).Last();
					return JToken.FromObject(val);
				}
				else if (val.startsWith("https://gs1.org/voc/"))
				{
					val = val.split(java.util.regex.Pattern.quote("/"), -1).Last();
					return JToken.FromObject(val);
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
//C# TO JAVA CONVERTER TASK: There is no equivalent in Java to the 'async' keyword:
//ORIGINAL LINE: internal static async Task<string> NormalizeEPCISJsonLDAsync(string jEPCISStr)
	public static Task<String> NormalizeEPCISJsonLDAsync(String jEPCISStr)
	{
		// convert into XDocument
		var settings = new JsonSerializerSettings();
		settings.DateParseHandling = DateParseHandling.None;
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: JObject json = JsonConvert.DeserializeObject<JObject>(jEPCISStr, settings) ?? throw new Exception("Failed to parse json from string. " + jEPCISStr);
		JObject json = JsonConvert.<JObject>DeserializeObject(jEPCISStr, settings) != null ? JsonConvert.<JObject>DeserializeObject(jEPCISStr, settings) : throw new RuntimeException("Failed to parse json from string. " + jEPCISStr);

//C# TO JAVA CONVERTER TASK: There is no equivalent to 'await' in Java:
		JObject jEPCISContext = await JsonContextHelper.GetJsonLDContextAsync("https://ref.gs1.org/standards/epcis/epcis-context.jsonld");
		HashMap<String, String> namespaces = JsonContextHelper.ScrapeNamespaces(jEPCISContext);

		JArray jEventList = json["epcisBody"] == null ? null : json["epcisBody"]["eventList"] instanceof JArray ? (JArray)json["epcisBody"]["eventList"] : null;
		if (jEventList == null)
		{
			Object tempVar = ((json["epcisBody"]["queryResults"] == null ? null : ((json["epcisBody"]["queryResults"]["resultsBody"] == null ? null : json["epcisBody"]["queryResults"]["resultsBody"]["eventList"]))));
			jEventList = json["epcisBody"] == null ? null : tempVar instanceof JArray ? (JArray)tempVar : null;
		}
		if (jEventList != null)
		{
			for (JObject jEvent : jEventList)
			{
				JsonContextHelper.ExpandVocab(jEvent, jEPCISContext, namespaces);
			}
		}

		return json.toString(Formatting.Indented);
	}
}
