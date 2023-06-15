package opentraceability.utility;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static okhttp3.HttpUrl.Companion.toHttpUrlOrNull;

public class JsonContextHelper {

    private static final Object lock = new Object();
    private static final ConcurrentMap<String, JSONObject> contextCache = new ConcurrentHashMap<>();

    public static JSONObject getJsonLDContext(String contextURL) throws Exception {
        JSONObject jContext = contextCache.get(contextURL);
        if (jContext == null) {
            HttpUrl httpUrl = toHttpUrlOrNull(contextURL);
            if (httpUrl == null) {
                throw new IllegalArgumentException("Invalid URL: " + contextURL);
            }
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(contextURL)
                    .build();
            Response response = client.newCall(request).execute();
            String jsonString = response.body().string();
            if (jsonString.isEmpty()) {
                throw new Exception("Failed to fetch JSON-LD context from " + contextURL + " : Response Body was empty.");
            }
            jContext = new JSONObject(jsonString);
            if (jContext != null) {
                contextCache.put(contextURL, jContext);
            } else {
                throw new Exception("Failed to fetch JSON-LD context from " + contextURL + " : Response Body was not in JSON format.");
            }
        }
        JSONObject jHead = jContext.optJSONObject("@context");
        if (jHead == null) {
            throw new Exception("Failed to fetch JSON-LD context from " + contextURL + " : \"@context\" field was not found.");
        }
        return jHead;
    }


    public static Map<String, String> scrapeNamespaces(JSONObject jContext) throws Exception {
        Map<String, String> namespaces = new HashMap<>();
        Iterator<String> keys = jContext.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object obj = jContext.get(key);
            if (obj instanceof JSONObject) {
                continue;
            }
            String value = Objects.toString(obj, "");
            if (value != null && isNamespace(value)) {
                namespaces.put(key, value.trim().replaceAll(":", ""));
            }
        }
        return namespaces;
    }

    public static boolean isNamespace(String value) {
        Pattern reg = Pattern.compile("^urn:[a-z0-9][a-z0-9-]{0,31}:[a-z0-9()+,\\-.:=@;\$_!*'%\\/?#]+$");
        if (value.matches("^https?://.*$")) {
            return true;
        } else {
            Matcher m = reg.matcher(value);
            return m.find();
        }
    }

    public static Object expandVocab(Object json, JSONObject jContext, Map<String, String> namespaces, JSONObject jVocabContext) throws Exception {
        return modifyVocab(json, jContext, namespaces, reverseMap(namespaces), JsonLDVocabTransformationType.Expand, jVocabContext);
    }

    public static Object compressVocab(Object json, JSONObject jContext, Map<String, String> namespaces, JSONObject jVocabContext) throws Exception {
        return modifyVocab(json, jContext, namespaces, reverseMap(namespaces), JsonLDVocabTransformationType.Compress, jVocabContext);
    }

    public static Object modifyVocab(Object json, JSONObject jContext, Map<String, String> namespaces, Map<String, String> namespacesReverse, JsonLDVocabTransformationType transformType, JSONObject jVocabContext) throws Exception {
        if (json instanceof JSONObject) {
            JSONObject jObj = (JSONObject) json;
            for (String key : jObj.keySet()) {
                JSONObject jContextProp = jContext.optJSONObject(key);
                if (jContextProp != null) {
                    Object jPropValue = jObj.opt(key);
                    Object jPropContext = jContextProp.opt("@context");
                    if (jPropValue != null && jPropContext != null) {
                        if (Objects.equals(jContextProp.optString("@type"), "@vocab")) {
                            if (jPropContext instanceof JSONObject) {
                                if (jPropValue instanceof JSONArray) {
                                    JSONArray jArr = (JSONArray) jPropValue;
                                    for (int i = 0; i < jArr.length(); i++) {
                                        Object jt = jArr.get(i);
                                        Object newValue = modifyVocab(jt, (JSONObject) jPropContext, namespaces, namespacesReverse, transformType, jVocabContext);
                                        if (newValue != null) {
                                            jArr.put(i, newValue);
                                        }
                                    }
                                } else {
                                    Object newValue = modifyVocab(jPropValue, (JSONObject) jPropContext, namespaces, namespacesReverse, transformType, jVocabContext);
                                    if (newValue != null) {
                                        jObj.put(key, newValue);
                                    }
                                }
                            } else {
                                throw new Exception("jContextProp has @type set to @vocab, but the @context is not a JSONObject. expected=" + JSONObject.class + " actual=" + jPropContext.getClass());
                            }
                        } else if (jPropValue instanceof JSONArray) {
                            JSONArray jArr = (JSONArray) jPropValue;
                            JSONObject jPropContextObj = jPropContext instanceof JSONArray ? ((JSONArray) jPropContext).getJSONObject(1) : (JSONObject) jPropContext;
                            for (int i = 0; i < jArr.length(); i++) {
                                Object jt = jArr.get(i);
                                if (jt instanceof JSONObject) {
                                    modifyVocab(jt, jPropContextObj, namespaces, namespacesReverse, transformType, jVocabContext);
                                } else {
                                    Object newValue = modifyVocab(jt, jPropContextObj, namespaces, namespacesReverse, transformType, jVocabContext);
                                    if (newValue != null) {
                                        jArr.put(i, newValue);
                                    }
                                }
                            }
                        } else if (jPropValue instanceof JSONObject) {
                            JSONObject jPropContextObj = jPropContext instanceof JSONArray ? ((JSONArray) jPropContext).getJSONObject(1) : (JSONObject) jPropContext;
                            modifyVocab(jPropValue, jPropContextObj, namespaces, namespacesReverse, transformType, jVocabContext);
                        }
                    }
                }
            }
            return jObj;
        } else {
            JSONObject jc = jVocabContext != null ? jVocabContext : jContext;
            String value = Objects.toString(json, "");
            if (value.isEmpty()) {
                return null;
            } else if (transformType == JsonLDVocabTransformationType.Expand) {
                Object jMapping = jc.opt(value);
                if (jMapping != null) {
                    String uri = jMapping.toString();
                    List<String> parts = Arrays.asList(uri.split(":"));
                    String ns = namespaces.get(parts.get(0));
                    if (ns == null) {
                        throw new Exception("VOCAB namespace does not exist for the parts[0]: " + parts.get(0) + ".");
                    }
                    String newValue = ns + parts.get(1);
                    return newValue;
                } else {
                    return null;
                }
            } else if (transformType == JsonLDVocabTransformationType.Compress) {
                String ns = value.substring(0, value.lastIndexOf('/') + 1);
                if (namespacesReverse.containsKey(ns)) {
                    String compressedValue = value.replace(ns, namespacesReverse.get(ns) + ":");
                    String compressedVocab = jc.keys().stream().filter(k -> Objects.equals(jc.optString(k), compressedValue)).findFirst().orElse(null);
                    return compressedVocab;
                } else {
                    return value;
                }
            } else {
                throw new Exception("Unrecognized JSON-LD Vocab Transformation Type = " + transformType);
            }
        }
    }

    public static boolean isComplexContext(JSONObject jObj) throws Exception {
        Iterator<String> keys = jObj.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object obj = jObj.opt(key);
            if (obj instanceof JSONObject) {
                return true;
            }
            String strValue = Objects.toString(obj, "");
            if (strValue.isEmpty() || !isNamespace(strValue)) {
                return true;
            }
        }
        return false;
    }

    public static <K