package opentraceability.mappers.masterdata;

import kotlin.reflect.KClass;
import opentraceability.interfaces.IMasterDataMapper;
import opentraceability.interfaces.IVocabularyElement;
import opentraceability.mappers.OpenTraceabilityJsonLDMapper;
import opentraceability.utility.JsonContextHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.HashMap;

public class GS1VocabJsonMapper implements IMasterDataMapper {
    @Override
    public String map(IVocabularyElement vocab) {
        if (vocab.context == null) {
            vocab.context = new JSONObject(
                "{\n" +
                "    \"cbvmda\": \"urn:epcglobal:cbvmda:mda\",\n" +
                "    \"xsd\": \"http://www.w3.org/2001/XMLSchema#\",\n" +
                "    \"gs1\": \"http://gs1.org/voc/\",\n" +
                "    \"@vocab\": \"http://gs1.org/voc/\",\n" +
                "    \"gdst\": \"https://traceability-dialogue.org/vocab\"\n" +
                "}"
            );
        }

        JSONObject context = vocab.getContext();
        Map<String, String> namespaces = getNamespaces(context != null ? context : throw new RuntimeException("vocab.getContext() is null."));
        Map<String, String> reversedNamespaces = new HashMap<>();

        for (Map.Entry<String, String> entry : namespaces.entrySet()) {
            reversedNamespaces.put(entry.getValue(), entry.getKey());
        }

        JSONObject json = OpenTraceabilityJsonLDMapper.toJson(vocab, reversedNamespaces);
        if (json == null) {
            throw new RuntimeException("Failed to map master data into GS1 web vocab.");
        }
        json.put("@context", vocab.getContext());
        return json.toString();
    }

    @Override
    public IVocabularyElement map(Type type, String value) {
        JSONObject json = new JSONObject(value);
        JSONObject context = json.optJSONObject("@context");
        if (context == null) {
            throw new RuntimeException("@context is null on the JSON-LD when deserializing GS1 Web Vocab. " + value);
        }
        Map<String, String> namespaces = getNamespaces(context);
        IVocabularyElement obj = (IVocabularyElement) OpenTraceabilityJsonLDMapper.fromJson(json, type, namespaces);
        obj.setContext(json.getJSONObject("@context"));
        return obj;
    }

    /*
    @Override
    public <T extends IVocabularyElement> IVocabularyElement map(String value) {
        return map(T.class, value);
    }
    */

    private Map<String, String> getNamespaces(Object jContext) {
        Map<String, String> namespaces = new HashMap<>();
        if (jContext instanceof JSONObject) {
            namespaces.putAll(JsonContextHelper.scrapeNamespaces((JSONObject)jContext));
        } else if (jContext instanceof JSONArray) {
            JSONArray jArray = (JSONArray)jContext;
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jObject = jArray.getJSONObject(i);
                Map<String, String> ns = JsonContextHelper.scrapeNamespaces(jObject);
                for (Map.Entry<String, String> entry : ns.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (!namespaces.containsKey(key)) {
                        namespaces.put(key, value);
                    }
                }
            }
        }
        return namespaces;
    }
}