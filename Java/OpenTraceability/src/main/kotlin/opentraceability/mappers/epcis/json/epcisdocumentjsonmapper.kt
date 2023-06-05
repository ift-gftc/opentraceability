package opentraceability.mappers.epcis.json

import opentraceability.interfaces.*
import opentraceability.mappers.*
import opentraceability.models.events.*
import org.json.*
import kotlin.reflect.KClass

class EPCISDocumentJsonMapper : IEPCISDocumentMapper {

    override fun map(strValue: String): EPCISDocument {
        try {
            val dic = EPCISDocumentBaseJsonMapper.readJSON<EPCISDocument>(strValue)

            var doc = dic.first
            var json = dic.second

            if (doc.epcisVersion != EPCISVersion.V2) {
                throw Exception("doc.epcisVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.")
            }

            // read the master data
            val jMasterData = json.optJSONObject("epcisHeader")?.optJSONObject("epcisMasterData")
            if (jMasterData != null) {
                EPCISJsonMasterDataReader.readMasterData(doc, jMasterData)
            }

            // read the events
            val jEventList = json.optJSONObject("epcisBody")?.optJSONArray("eventList")
            if (jEventList != null) {
                for (i in 0 until jEventList.length()) {
                    val jEvent = jEventList.optJSONObject(i)
                    val eventType = EPCISDocumentBaseJsonMapper.getEventTypeFromProfile(jEvent)
                    val e = OpenTraceabilityJsonLDMapper.fromJson(jEvent, eventType as KClass<*>, doc.namespaces) as IEvent
                    doc.events.add(e)
                }
            }

            return doc
        } catch (ex: Exception) {
            val exception = Exception("Failed to parse the EPCIS Document from the JSON-LD. json-ld=$strValue", ex)
            opentraceability.OTLogger.error(exception)
            throw exception
        }
    }

    override fun map(doc: EPCISDocument): String {
        if (doc.epcisVersion != EPCISVersion.V2) {
            throw Exception("doc.epcisVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.")
        }

        val epcisNS = if (doc.epcisVersion == EPCISVersion.V2) opentraceability.Constants.EPCIS_2_NAMESPACE else opentraceability.Constants.EPCIS_1_NAMESPACE

        val namespacesReversed = doc.namespaces.entries.associate { (key, value) -> value to key }.toMutableMap()


        val jEventList = JSONArray()
        val jEventBody = JSONObject()
        jEventBody.put("eventList", jEventList)
        for (e in doc.events) {
            val jEvent = OpenTraceabilityJsonLDMapper.toJson(e, namespacesReversed)
            if (jEvent != null) {
                jEventList.put(jEvent)
            }
        }

        val json = EPCISDocumentBaseJsonMapper.writeJson(doc, epcisNS, "EPCISDocument")

        if (!doc.header?.Sender?.Identifier.isNullOrBlank()) {
            json.put("sender", doc.header?.Sender?.Identifier)
        }

        if (!doc.header?.Receiver?.Identifier.isNullOrBlank()) {
            json.put("receiver", doc.header?.Receiver?.Identifier)
        }

        if (!doc.header?.DocumentIdentification?.InstanceIdentifier.isNullOrBlank()) {
            json.put("instanceIdentifier", doc.header?.DocumentIdentification?.InstanceIdentifier)
        }

        EPCISJsonMasterDataWriter.writeMasterData(json, doc)

        json.put("epcisBody", jEventBody)

        // conform the JSON-LD to the compacted version with CURIE's that EPCIS 2.0 likes
        EPCISDocumentBaseJsonMapper.conformEPCISJsonLD(json, doc.namespaces)

        // validate the JSON-LD schema
        EPCISDocumentBaseJsonMapper.checkSchema(json)

        return json.toString(4)
    }
}