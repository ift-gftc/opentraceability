package opentraceability.mappers.masterdata;

import Newtonsoft.Json.Linq.*;
import opentraceability.interfaces.*;
import opentraceability.utility.*;
import opentraceability.*;
import opentraceability.mappers.*;
import java.util.*;

public class GS1VocabJsonMapper implements IMasterDataMapper
{
	public final String Map(IVocabularyElement vocab)
	{
		if (vocab.getContext() == null)
		{
			vocab.setContext(JSONObject.Parse("{" + "\r\n" + 
"                                    \"cbvmda\": \"urn:epcglobal:cbvmda:mda\"," + "\r\n" + 
"                                    \"xsd\": \"http://www.w3.org/2001/XMLSchema#\"," + "\r\n" + 
"                                    \"gs1\": \"http://gs1.org/voc/\"," + "\r\n" + 
"                                    \"@vocab\": \"http://gs1.org/voc/\"," + "\r\n" + 
"                                    \"gdst\": \"https://traceability-dialogue.org/vocab\"" + "\r\n" + 
"                                }"));
		}

//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: Dictionary<string, string> namespaces = GetNamespaces(vocab.Context ?? throw new Exception("vocab.Context is null."));
		HashMap<String, String> namespaces = GetNamespaces(vocab.getContext() != null ? vocab.getContext() : throw new RuntimeException("vocab.Context is null."));
		System.Nullable<Object> tempVar = OpenTraceabilityJsonLDMapper.ToJson(vocab, namespaces.Reverse());
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: JSONObject json = tempVar instanceof JSONObject ? (JSONObject)tempVar : null ?? throw new Exception("Failed to map master data into GS1 web vocab.");
		JSONObject json = tempVar instanceof JSONObject ? (JSONObject)tempVar : (null != null ? null : throw new RuntimeException("Failed to map master data into GS1 web vocab."));
		json["@context"] = vocab.getContext();
		return json.toString();
	}

	public final <T extends IVocabularyElement> IVocabularyElement Map(String value)
	{
		return Map(T.class, value);
	}

	public final IVocabularyElement Map(Type type, String value)
	{
		JSONObject json = JSONObject.Parse(value);
//C# TO JAVA CONVERTER TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: Dictionary<string, string> namespaces = GetNamespaces(json["@context"] ?? throw new Exception("@context is null on the JSON-LD when deserializing GS1 Web Vocab. " + value));
		HashMap<String, String> namespaces = GetNamespaces(json["@context"] != null ? json["@context"] : throw new RuntimeException("@context is null on the JSON-LD when deserializing GS1 Web Vocab. " + value));
		IVocabularyElement obj = (IVocabularyElement)OpenTraceabilityJsonLDMapper.FromJson(json, type, namespaces);
		obj.setContext(json["@context"]);
		return obj;
	}

	private HashMap<String, String> GetNamespaces(Object jContext)
	{
		// build our namespaces
		HashMap<String, String> namespaces = new HashMap<String, String>();
		if (jContext != null)
		{
			if (jContext instanceof JSONObject)
			{
				namespaces = JsonContextHelper.ScrapeNamespaces((JSONObject)jContext);
			}
			else if (jContext instanceof JSONArray)
			{
				for (JSONObject j : (JSONArray)jContext)
				{
					var ns = JsonContextHelper.ScrapeNamespaces(j);
					for (var kvp : ns.entrySet())
					{
						if (!namespaces.containsKey(kvp.getKey()))
						{
							namespaces.put(kvp.getKey(), kvp.getValue());
						}
					}
				}
			}
		}
		return namespaces;
	}
}
