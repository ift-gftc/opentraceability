package mappers.epcis.json

import interfaces.*
import mappers.*
import models.events.*
import org.json.*

class EPCISDocumentJsonMapper : IEPCISDocumentMapper {

    override fun Map(strValue: String): EPCISDocument {
        try {
            val dic = EPCISDocumentBaseJsonMapper.readJSON<EPCISDocument>(strValue)

            var doc = dic.first
            var json = dic.second

            if (doc.EPCISVersion != EPCISVersion.V2) {
                throw Exception("doc.EPCISVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.")
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
                    val e = OpenTraceabilityJsonLDMapper.fromJson(jEvent, eventType, doc.Namespaces) as IEvent
                    doc.Events.add(e)
                }
            }

            return doc
        } catch (ex: Exception) {
            val exception = Exception("Failed to parse the EPCIS Document from the JSON-LD. json-ld=$strValue", ex)
            OTLogger.error(exception)
            throw exception
        }
    }




    override fun Map(doc: EPCISDocument): String {
        if (doc.EPCISVersion != EPCISVersion.V2) {
            throw Exception("doc.EPCISVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.")
        }

        val epcisNS = if (doc.EPCISVersion == EPCISVersion.V2) Constants.EPCIS_2_NAMESPACE else Constants.EPCIS_1_NAMESPACE

        val namespacesReversed = doc.Namespaces.entries.associate { (key, value) -> value to key }.toMutableMap()


        val jEventList = JSONArray()
        val jEventBody = JSONObject()
        jEventBody.put("eventList", jEventList)
        for (e in doc.Events) {
            val jEvent = OpenTraceabilityJsonLDMapper.toJson(e, namespacesReversed)
            if (jEvent != null) {
                jEventList.put(jEvent)
            }
        }

        val json = EPCISDocumentBaseJsonMapper.writeJson(doc, epcisNS, "EPCISDocument")

        if (!doc.Header?.Sender?.Identifier.isNullOrBlank()) {
            json.put("sender", doc.Header?.Sender?.Identifier)
        }

        if (!doc.Header?.Receiver?.Identifier.isNullOrBlank()) {
            json.put("receiver", doc.Header?.Receiver?.Identifier)
        }

        if (!doc.Header?.DocumentIdentification?.InstanceIdentifier.isNullOrBlank()) {
            json.put("instanceIdentifier", doc.Header?.DocumentIdentification?.InstanceIdentifier)
        }

        EPCISJsonMasterDataWriter.writeMasterData(json, doc)

        json.put("epcisBody", jEventBody)

        // conform the JSON-LD to the compacted version with CURIE's that EPCIS 2.0 likes
        EPCISDocumentBaseJsonMapper.conformEPCISJsonLD(json, doc.Namespaces)

        // validate the JSON-LD schema
        EPCISDocumentBaseJsonMapper.checkSchema(json)

        return json.toString(4)
    }

}
