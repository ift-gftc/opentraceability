package opentraceability.mappers.epcis.json;

import opentraceability.Constants;
import opentraceability.OTLogger;
import opentraceability.interfaces.IEvent;
import opentraceability.interfaces.IEPCISQueryDocumentMapper;
import opentraceability.mappers.EPCISDocumentBaseJsonMapper;
import opentraceability.mappers.OpenTraceabilityJsonLDMapper;
import opentraceability.models.events.EPCISQueryDocument;
import opentraceability.models.events.EPCISVersion;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class EPCISQueryDocumentJsonMapper implements IEPCISQueryDocumentMapper {

    @Override
    public EPCISQueryDocument map(String strValue, boolean checkSchema) {
        try {
            Map<EPCISDocumentBaseJsonMapper.Field, Object> result = EPCISDocumentBaseJsonMapper.readJSON(strValue, checkSchema);
            EPCISQueryDocument doc = (EPCISQueryDocument) result.get(EPCISDocumentBaseJsonMapper.Field.DOC);
            JSONObject json = (JSONObject) result.get(EPCISDocumentBaseJsonMapper.Field.JSON);

            if (doc.epcisVersion != EPCISVersion.V2) {
                throw new Exception("doc.epcisVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.");
            }

            doc.setQueryName(json.getJSONObject("epcisBody").getJSONObject("queryResults").getString("queryName"));
            doc.setSubscriptionID(json.getJSONObject("epcisBody").getJSONObject("queryResults").getString("subscriptionID"));

            JSONArray jEventsList = json.getJSONObject("epcisBody").getJSONObject("queryResults").getJSONObject("resultsBody").getJSONArray("eventList");
            if (jEventsList != null) {
                for (int i = 0; i < jEventsList.length(); i++) {
                    JSONObject jEvent = jEventsList.getJSONObject(i);
                    String eventType = EPCISDocumentBaseJsonMapper.getEventTypeFromProfile(jEvent);
                    IEvent e = (IEvent) OpenTraceabilityJsonLDMapper.fromJson(jEvent, eventType, doc.getNamespaces());
                    doc.events.add(e);
                }
            }
            return doc;
        } catch (Exception ex) {
            Exception exception = new Exception("Failed to parse the EPCIS document from the XML. xml=" + strValue, ex);
            OTLogger.error(exception);
            throw exception;
        }
    }

    @Override
    public String map(EPCISQueryDocument doc) {
        if (doc.getEpcisVersion() != EPCISVersion.V2) {
            throw new Exception("doc.epcisVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.");
        }
        String epcisNS = doc.getEpcisVersion() == EPCISVersion.V2 ? Constants.EPCISQUERY_2_NAMESPACE : Constants.EPCISQUERY_1_NAMESPACE;

        Map<String, String> namespacesReversed = EPCISDocumentBaseJsonMapper.reverseNamespaces(doc.getNamespaces());

        JSONArray jEventsList = new JSONArray();
        for (IEvent e : doc.events) {
            JSONObject jEvent = (JSONObject) OpenTraceabilityJsonLDMapper.toJson(e, namespacesReversed);
            if (jEvent != null) {
                jEventsList.put(jEvent);
            }
        }

        JSONObject json = EPCISDocumentBaseJsonMapper.writeJson(doc, epcisNS, "EPCISQueryDocument");
        JSONObject jEPCISBody = new JSONObject();
        JSONObject jQueryResults = new JSONObject();
        JSONObject jResultsBody = new JSONObject();

        jQueryResults.put("queryName", doc.getQueryName().toString());
        jQueryResults.put("subscriptionID", doc.getSubscriptionID().toString());
        jResultsBody.put("eventList", jEventsList);
        jQueryResults.put("resultsBody", jResultsBody);
        jEPCISBody.put("queryResults", jQueryResults);
        json.put("epcisBody", jEPCISBody);

        EPCISDocumentBaseJsonMapper.conformEPCISJsonLD(json, doc.getNamespaces());
        EPCISDocumentBaseJsonMapper.checkSchema(json);
        return json.toString();
    }
}