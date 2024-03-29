package opentraceability.mappers.epcis.json

import opentraceability.interfaces.*
import opentraceability.mappers.OpenTraceabilityJsonLDMapper
import opentraceability.models.events.*
import org.json.*

class EPCISQueryDocumentJsonMapper : IEPCISQueryDocumentMapper {
    override fun map(strValue: String, checkSchema: Boolean): EPCISQueryDocument {
        try {
            val (doc, json) = EPCISDocumentBaseJsonMapper.readJSON<EPCISQueryDocument>(strValue, checkSchema)

            if (doc.epcisVersion != EPCISVersion.V2) {
                throw Exception("doc.epcisVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.")
            }

            doc.QueryName = json?.getJSONObject("epcisBody")?.getJSONObject("queryResults")?.getString("queryName") ?: ""
            doc.SubscriptionID = json?.getJSONObject("epcisBody")?.getJSONObject("queryResults")?.getString("subscriptionID") ?: ""

            val jEventsList = json?.getJSONObject("epcisBody")?.getJSONObject("queryResults")?.getJSONObject("resultsBody")?.getJSONArray("eventList")
            if (jEventsList != null) {
                for (i in 0 until jEventsList.length()) {
                    val jEvent = jEventsList.getJSONObject(i)
                    val eventType = EPCISDocumentBaseJsonMapper.getEventTypeFromProfile(jEvent)
                    val e = OpenTraceabilityJsonLDMapper.fromJson(jEvent, eventType, doc.namespaces) as IEvent
                    doc.events.add(e)
                }
            }
            return doc
        } catch (ex: Exception) {
            val exception = Exception("Failed to parse the EPCIS document from the XML. xml=$strValue", ex)
            opentraceability.OTLogger.error(exception)
            throw exception
        }
    }

    override fun map(doc: EPCISQueryDocument): String {
        if (doc.epcisVersion != EPCISVersion.V2) {
            throw Exception("doc.epcisVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.")
        }
        val epcisNS = if (doc.epcisVersion == EPCISVersion.V2) opentraceability.Constants.EPCISQUERY_2_NAMESPACE else opentraceability.Constants.EPCISQUERY_1_NAMESPACE

        val namespacesReversed = doc.namespaces.entries.reversed().associateBy({it.key}, {it.value}).toMutableMap()

        val jEventsList = JSONArray()
        for (e in doc.events) {
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
        EPCISDocumentBaseJsonMapper.conformEPCISJsonLD(json, doc.namespaces)
        EPCISDocumentBaseJsonMapper.checkSchema(json)
        return json.toString()
    }
}
