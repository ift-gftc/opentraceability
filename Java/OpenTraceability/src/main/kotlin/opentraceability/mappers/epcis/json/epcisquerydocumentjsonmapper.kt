package mappers.epcis.json

import interfaces.*
import mappers.OpenTraceabilityJsonLDMapper
import models.events.*
import org.json.*

class EPCISQueryDocumentJsonMapper : IEPCISQueryDocumentMapper {
    override fun map(strValue: String, checkSchema: Boolean): EPCISQueryDocument {
        try {
            val (doc, json) = EPCISDocumentBaseJsonMapper.readJSON<EPCISQueryDocument>(strValue, checkSchema)

            if (doc.EPCISVersion != EPCISVersion.V2) {
                throw Exception("doc.EPCISVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.")
            }

            doc.QueryName = json["epcisBody"]?.get("queryResults")?.get("queryName")?.toString() ?: ""
            doc.SubscriptionID = json["epcisBody"]?.get("queryResults")?.get("subscriptionID")?.toString() ?: ""
            val jEventsList = json["epcisBody"]?.get("queryResults")?.get("resultsBody")?.get("eventList") as JSONArray?
            if (jEventsList != null) {
                for (jEvent in jEventsList) {
                    val eventType = EPCISDocumentBaseJsonMapper.getEventTypeFromProfile(jEvent)
                    val e = OpenTraceabilityJsonLDMapper.fromJson(jEvent, eventType, doc.Namespaces) as IEvent
                    doc.Events.add(e)
                }
            }
            return doc
        } catch (ex: Exception) {
            val exception = Exception("Failed to parse the EPCIS document from the XML. xml=$strValue", ex)
            OTLogger.error(exception)
            throw exception
        }
    }

    override fun map(doc: EPCISQueryDocument): String {
        if (doc.EPCISVersion != EPCISVersion.V2) {
            throw Exception("doc.EPCISVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.")
        }
        val epcisNS = if (doc.EPCISVersion == EPCISVersion.V2) Constants.EPCISQUERY_2_NAMESPACE else Constants.EPCISQUERY_1_NAMESPACE

        val namespacesReversed = doc.Namespaces.entries.reversed().associateBy({it.key}, {it.value})

        val jEventsList = JSONArray()
        for (e in doc.Events) {
            val jEvent = OpenTraceabilityJsonLDMapper.toJson(e, namespacesReversed) as JSONObject?
            if (jEvent != null) {
                jEventsList.put(jEvent)
            }
        }
        val json = EPCISDocumentBaseJsonMapper.writeJson(doc, epcisNS, "EPCISQueryDocument")
        val jEPCISBody = JSONObject()
        val jQueryResults = JSONObject()
        val jResultsBody = JSONObject()

        jQueryResults.put("queryName",doc.QueryName)
        jQueryResults.put("subscriptionID",doc.SubscriptionID)
        jResultsBody.put("eventList",jEventsList)
        jQueryResults.put("resultsBody",jResultsBody)
        jEPCISBody.put("queryResults",jQueryResults)
        json.put("epcisBody",jEPCISBody)
        EPCISDocumentBaseJsonMapper.conformEPCISJsonLD(json, doc.Namespaces)
        EPCISDocumentBaseJsonMapper.checkSchema(json)
        return json.toString()
    }
}
