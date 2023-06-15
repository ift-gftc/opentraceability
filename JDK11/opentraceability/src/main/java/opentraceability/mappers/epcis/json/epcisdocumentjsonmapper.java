package opentraceability.mappers.epcis.json;

import org.json.JSONArray;
import org.json.JSONObject;

import kotlin.reflect.KClass;
import opentraceability.Constants;
import opentraceability.OTLogger;
import opentraceability.interfaces.IEvent;
import opentraceability.interfaces.IEPCISDocumentMapper;
import opentraceability.mappers.EPCISDocumentBaseJsonMapper;
import opentraceability.mappers.epcis.json.data.EPCISJsonMasterDataReader;
import opentraceability.mappers.epcis.json.data.EPCISJsonMasterDataWriter;
import opentraceability.mappers.epcis.json.ld.OpenTraceabilityJsonLDMapper;
import opentraceability.mappers.epcis.json.schema.EPCISDocumentBaseJsonSchemaValidator;
import opentraceability.models.enums.EPCISVersion;
import opentraceability.models.events.EPCISDocument;
import opentraceability.utils.JsonUtils;

public class EPCISDocumentJsonMapper implements IEPCISDocumentMapper {

    public EPCISDocument map(final String strValue) {
        try {
            final var dic = EPCISDocumentBaseJsonMapper.readJSON(EPCISDocument.class, strValue, false);

            final var doc = dic.getFirst();
            final var json = dic.getSecond();

            if (doc.epcisVersion != EPCISVersion.V2) {
                throw new Exception("doc.epcisVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.");
            }

            final var jMasterData = JsonUtils.getJSONObject(json.optJSONObject("epcisHeader"), "epcisMasterData");
            if (jMasterData != null) {
                EPCISJsonMasterDataReader.readMasterData(doc, jMasterData);
            }

            final var jEventList = JsonUtils.getJSONArray(JsonUtils.getJSONObject(json, "epcisBody"), "eventList");
            if (jEventList != null) {
                for (var i = 0; i < jEventList.length(); i++) {
                    final var jEvent = JsonUtils.getJSONObject(jEventList, i);
                    final var eventType = EPCISDocumentBaseJsonMapper.getEventTypeFromProfile(jEvent);
                    final var e = OpenTraceabilityJsonLDMapper.fromJson(jEvent, (Type)eventType, doc.namespaces);
                    doc.events.add((IEvent)e);
                }
            }

            return doc;
        } catch (final Exception ex) {
            final var exception = new Exception("Failed to parse the EPCIS Document from the JSON-LD. json-ld=" + strValue, ex);
            OTLogger.error(exception);
            throw exception;
        }
    }

    public String map(final EPCISDocument doc) {
        if (doc.epcisVersion != EPCISVersion.V2) {
            throw new Exception("doc.epcisVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.");
        }

        final var epcisNS = doc.epcisVersion == EPCISVersion.V2 ? Constants.EPCIS_2_NAMESPACE : Constants.EPCIS_1_NAMESPACE;

        final var namespacesReversed = JsonUtils.reverseConvertHashMap(doc.namespaces);

        final var jEventList = new JSONArray();
        final var jEventBody = new JSONObject();
        JsonUtils.put(jEventBody, "eventList", jEventList);
        for (final var e : doc.events) {
            final var jEvent = OpenTraceabilityJsonLDMapper.toJson(e, namespacesReversed);
            if (jEvent != null) {
                jEventList.put(jEvent);
            }
        }

        final var json = EPCISDocumentBaseJsonMapper.writeJson(doc, epcisNS, "EPCISDocument");

        if (!JsonUtils.getStringValue(JsonUtils.getJSONObject(json, "header"), "Sender", "Identifier").isBlank()) {
            JsonUtils.put(json, "sender", JsonUtils.getStringValue(JsonUtils.getJSONObject(json, "header"), "Sender", "Identifier"));
        }

        if (!JsonUtils.getStringValue(JsonUtils.getJSONObject(json, "header"), "Receiver", "Identifier").isBlank()) {
            JsonUtils.put(json, "receiver", JsonUtils.getStringValue(JsonUtils.getJSONObject(json, "header"), "Receiver", "Identifier"));
        }

        if (!JsonUtils.getStringValue(JsonUtils.getJSONObject(json, "header"), "DocumentIdentification", "InstanceIdentifier").isBlank()) {
            JsonUtils.put(json, "instanceIdentifier", JsonUtils.getStringValue(JsonUtils.getJSONObject(json, "header"), "DocumentIdentification", "InstanceIdentifier"));
        }

        EPCISJsonMasterDataWriter.writeMasterData(json, doc);

        JsonUtils.put(json, "epcisBody", jEventBody);

        EPCISDocumentBaseJsonMapper.conformEPCISJsonLD(json, doc.namespaces);

        EPCISDocumentBaseJsonSchemaValidator.checkSchema(json);

        return json.toString(4);
    }
}