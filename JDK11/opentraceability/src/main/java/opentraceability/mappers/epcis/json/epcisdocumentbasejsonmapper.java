package opentraceability.mappers.epcis.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import opentraceability.Setup;
import opentraceability.interfaces.IEvent;
import opentraceability.models.common.*;
import opentraceability.models.events.*;
import opentraceability.utility.*;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import kotlin.reflect.KClass;

public class EPCISDocumentBaseJsonMapper {
    public static <T extends EPCISBaseDocument> Pair<T, JSONObject> readJSON(String strValue, boolean checkSchema) throws Exception {
        if (checkSchema) {
            JSONObject obj = new JSONObject(strValue);
            checkSchema(obj);
        }

        String normalizedStrValue = normalizeEPCISJsonLD(strValue);

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
        JsonObject json = gson.fromJson(normalizedStrValue, JsonObject.class);

        T document = (T) getInstanceFromClass(T.class);

        document.attributes.put("schemaVersion",
                (json.get("schemaVersion") != null ? json.get("schemaVersion").getAsString() : ""));
        document.epcisVersion = EPCISVersion.V2;

        String creationDateAttributeStr = (json.get("creationDate") != null ? json.get("creationDate").getAsString() : "");
        if (!creationDateAttributeStr.isEmpty()) {
            document.creationDate = StringExtensions.tryConvertToDateTimeOffset(creationDateAttributeStr);
        }

        document.attributes = new HashMap<String, Object>();

        JsonArray jContextArray = json.getAsJsonArray("@context");

        if (jContextArray != null) {
            for (int i = 0; i < jContextArray.size(); i++) {
                Object jt = jContextArray.get(i);
                if (jt instanceof JsonObject) {
                    Map<String, String> ns = JsonContextHelper.scrapeNamespaces(new JSONObject(jt.toString()));
                    for (Map.Entry<String, String> entry : ns.entrySet()) {
                        if (!document.namespaces.containsKey(entry.getKey())) {
                            document.namespaces.put(entry.getKey(), entry.getValue().toString());
                        }
                    }
                    document.contexts.add(jt.toString());
                } else {
                    String value = jt.toString();
                    if (!value.isEmpty()) {
                        String newValue = value.replace("\"", "");
                        String context = JsonContextHelper.getJsonLDContext(newValue);
                        Map<String, String> ns = JsonContextHelper.scrapeNamespaces(new JSONObject(context));
                        for (Map.Entry<String, String> entry : ns.entrySet()) {
                            if (!document.namespaces.containsKey(entry.getKey())) {
                                document.namespaces.put(entry.getKey(), entry.getValue().toString());
                            }
                        }
                        document.contexts.add(newValue);
                    }
                }
            }
        } else {
            throw new Exception(
                    "the @context on the root of the JSON-LD EPCIS file was not an array. we are currently expecting this to be an array.");
        }

        if (json.get("id") != null) {
            document.attributes.put("id", json.get("id").getAsString());
        }

        document.header = new StandardBusinessDocumentHeader();

        document.header.Sender = new SBDHOrganization();
        document.header.Sender.Identifier = (json.get("sender") != null ? json.get("sender").getAsString() : "");

        document.header.Receiver = new SBDHOrganization();
        document.header.Receiver.Identifier = (json.get("receiver") != null ? json.get("receiver").getAsString()
                : "");

        document.header.DocumentIdentification = new SBDHDocumentIdentification();
        document.header.DocumentIdentification.InstanceIdentifier = (json.get("instanceIdentifier") != null
                ? json.get("instanceIdentifier").getAsString() : "");

        JSONObject jResult = new JSONObject(json.toString());
        return new Pair<>(document, jResult);
    }

    public static JSONObject writeJson(EPCISBaseDocument doc, String epcisNS, String docType) throws Exception {
        if (doc.epcisVersion != EPCISVersion.V2) {
            throw new Exception("doc.epcisVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.");
        }

        JSONObject jobj = new JSONObject();
        jobj.put("@context", doc.contexts);
        jobj.put("@type", docType);

        jobj.put("schemaVersion", doc.attributes.get("schemaVersion"));
        jobj.put("epcisVersion", "2.0");
        jobj.put("id", doc.attributes.get("id"));

        jobj.put("creationDate", doc.creationDate);

        jobj.put("sender", doc.header.Sender.Identifier);
        jobj.put("receiver", doc.header.Receiver.Identifier);
        jobj.put("instanceIdentifier", doc.header.DocumentIdentification.InstanceIdentifier);
        return jobj;
    }

    public static Type getEventTypeFromProfile(JSONObject jEvent) throws Exception {
        EventAction action = null;
        String bizStep = null;
        String eventType = jEvent.optString("type");

        ArrayList<EventProfile> profiles = new ArrayList<>();

        for (EventProfile profile : Setup.Profiles) {
            if (profile.EventType.toString().equals(eventType)) {
                if (profile.Action == null || profile.Action == action) {
                    if (profile.BusinessStep == null || profile.BusinessStep.equalsIgnoreCase(bizStep)) {
                        profiles.add(profile);
                    }
                }
            }
        }

        profiles.sort((p1, p2) -> p2.SpecificityScore - p1.SpecificityScore);

        if (profiles.isEmpty() == true) {
            throw new Exception(
                    "Failed to create event from profile. Type=" + eventType + " and BizStep=" + bizStep + " and Action="
                            + action);
        } else {
            for (EventProfile profile : profiles) {
                if (profile.KDEProfiles != null) {
                    for (KeyedDataElementProfile kdeProfile : profile.KDEProfiles) {
                        if (jEvent.opt(kdeProfile.JPath) == null) {
                            profiles.remove(profile);
                            break;
                        } else {
                            break;
                        }
                    }
                } else {
                    continue;
                }
            }

            if (profiles.isEmpty() == true) {
                throw new Exception(
                        "Failed to create event from profile