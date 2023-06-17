package opentraceability.mappers.masterdata;

import opentraceability.interfaces.*;
import opentraceability.utility.*;
import opentraceability.*;
import opentraceability.mappers.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class GS1VocabJsonMapper implements IMasterDataMapper
{
	public String map(IVocabularyElement vocab) throws Exception {
		if (vocab.context == null)
		{
			vocab.context = new JSONObject("{" + "\r\n" + 
"                                    \"cbvmda\": \"urn:epcglobal:cbvmda:mda\"," + "\r\n" + 
"                                    \"xsd\": \"http://www.w3.org/2001/XMLSchema#\"," + "\r\n" + 
"                                    \"gs1\": \"http://gs1.org/voc/\"," + "\r\n" + 
"                                    \"@vocab\": \"http://gs1.org/voc/\"," + "\r\n" + 
"                                    \"gdst\": \"https://traceability-dialogue.org/vocab\"" + "\r\n" + 
"                                }");
		}

		Map<String, String> namespaces = GetNamespaces(vocab.context);
		Object obj = OpenTraceabilityJsonLDMapper.ToJson(vocab, DictionaryExtensions.reverse(namespaces));

		JSONObject json = obj instanceof JSONObject ? (JSONObject)obj : null;
		json.put("@context", vocab.context);
		return json.toString();
	}

	public final <T extends IVocabularyElement> IVocabularyElement map(String value, Class<T> clazz) throws Exception {
		return map(clazz, value);
	}

	public final IVocabularyElement map(java.lang.reflect.Type type, String value) throws Exception {
		JSONObject json = new JSONObject(value);

		JSONObject jContext = JSONExtensions.queryForObject(json,"@context");
		if (jContext == null)
		{
			throw new Exception("Failed to find @context.");
		}
		Map<String, String> namespaces = GetNamespaces(jContext);
		IVocabularyElement obj = (IVocabularyElement)OpenTraceabilityJsonLDMapper.FromJson(json, type, namespaces);
		obj.context = jContext;
		return obj;
	}

	private Map<String, String> GetNamespaces(Object jContext) throws Exception {

		// build our namespaces
		Map<String, String> namespaces = new HashMap<String, String>();
		if (jContext != null)
		{
			if (jContext instanceof JSONObject)
			{
				namespaces = JsonContextHelper.scrapeNamespaces((JSONObject)jContext);
			}
			else if (jContext instanceof JSONArray)
			{
				for (Object o : (JSONArray)jContext)
				{
					if (o instanceof JSONObject)
					{
						JSONObject j = (JSONObject) o;
						var ns = JsonContextHelper.scrapeNamespaces(j);
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
		}
		return namespaces;
	}
}
