package mappers.epcis.json

import interfaces.IEPCISDocumentMapper
import interfaces.IEvent
import mappers.OpenTraceabilityJsonLDMapper
import models.events.*
import models.events.EPCISDocument
import org.json.simple.JSONArray
import org.json.simple.JSONObject

class EPCISDocumentJsonMapper : IEPCISDocumentMapper {

    override fun Map(strValue: String): EPCISDocument {
        try {
            val (doc, json) = EPCISDocumentBaseJsonMapper.ReadJSON<EPCISDocument>(strValue)

            if (doc.epcisVersion != EPCISVersion.V2) {
                throw Exception("doc.epcisVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.")
            }

            // read the master data
            val jMasterData = json.optJSONObject("epcisHeader")?.optJSONObject("epcisMasterData")
            jMasterData?.let { EPCISJsonMasterDataReader.ReadMasterData(doc, it) }

            // read the events
            val jEventList = json.optJSONObject("epcisBody")?.optJSONArray("eventList")
            jEventList?.let { array ->
                for (i in 0 until array.length()) {
                    val jEvent = array.getJSONObject(i)
                    val eventType = EPCISDocumentBaseJsonMapper.getEventTypeFromProfile(jEvent)
                    val e = OpenTraceabilityJsonLDMapper.FromJson<IEvent>(jEvent, eventType, doc.namespaces)
                    doc.events.add(e)
                }
            }

            return doc
        } catch (ex: Exception) {
            val exception = Exception("Failed to parse the EPCIS Document from the JSON-LD. json-ld=$strValue", ex)
            OTLogger.Error(exception)
            throw exception
        }

    }
    override fun Map(doc: EPCISDocument): String {
        if (doc.EPCISVersion != EPCISVersion.V2) {
            throw Exception("doc.epcisVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.")
        }

        val epcisNS = if (doc.EPCISVersion == EPCISVersion.V2) Constants.EPCIS_2_NAMESPACE else Constants.EPCIS_1_NAMESPACE

        val namespacesReversed = doc.Namespaces.asReversed()

// write the events
        val jEventList = JSONArray()
        val jEventBody = JSONObject()
        jEventBody.put("eventList", jEventList)
        for (e in doc.Events) {
            val jEvent = OpenTraceabilityJsonLDMapper.ToJson(e, namespacesReversed)
            jEvent?.let { jEventList.put(it) }
        }

        var json = EPCISDocumentBaseJsonMapper.writeJson(doc, epcisNS, "EPCISDocument")

// write the header
        if (!doc.Header?.Sender?.Identifier.isNullOrEmpty()) {
            json.put("sender", doc.Header?.Sender?.Identifier)
        }

        if (!doc.Header?.Receiver?.Identifier.isNullOrEmpty()) {
            json.put("receiver", doc.Header?.Receiver?.Identifier)
        }

        if (!doc.Header?.DocumentIdentification?.InstanceIdentifier.isNullOrEmpty()) {
            json.put("instanceIdentifier", doc.Header?.DocumentIdentification?.InstanceIdentifier)
        }

        EPCISJsonMasterDataWriter.WriteMasterData(json, doc)

        json.put("epcisBody", jEventBody)

// conform the JSON-LD to the compacted version with CURIE's that EPCIS 2.0 likes
        EPCISDocumentBaseJsonMapper.conformEPCISJsonLD(json.toString(), doc.Namespaces)

// validate the JSON-LD schema
        EPCISDocumentBaseJsonMapper.checkSchema(json)

        return json.toString(4) // 4 is the standard number of spaces for indent

    }
}
