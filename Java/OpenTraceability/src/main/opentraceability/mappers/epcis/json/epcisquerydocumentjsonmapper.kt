package mappers.epcis.json

import interfaces.IEPCISQueryDocumentMapper
import mappers.OpenTraceabilityJsonLDMapper
import models.events.EPCISQueryDocument
import models.events.EPCISVersion

class EPCISQueryDocumentJsonMapper : IEPCISQueryDocumentMapper {
    fun Map(strValue: String, checkSchema: Boolean = true): EPCISQueryDocument {
        try {
            val doc = EPCISDocumentBaseJsonMapper.ReadJSON<EPCISQueryDocument>(strValue, checkSchema)
            if (doc.EpcisVersion != EPCISVersion.V2) {
                throw Exception("doc.epcisVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.")
            }
            doc.QueryName = json["epcisBody"]?.get("queryResults")?.get("queryName")?.toString() ?: ""
            doc.SubscriptionID = json["epcisBody"]?.get("queryResults")?.get("subscriptionID")?.toString() ?: ""
            val jEventsList = json["epcisBody"]?.get("queryResults")?.get("resultsBody")?.get("eventList") as JArray?
            if (jEventsList != null) {
                for (jEvent in jEventsList) {
                    val eventType = EPCISDocumentBaseJsonMapper.getEventTypeFromProfile(jEvent)
                    val e = OpenTraceabilityJsonLDMapper.FromJson(jEvent, eventType, doc.Namespaces) as IEvent
                    doc.Events.add(e)
                }
            }
            return doc
        } catch (ex: Exception) {
            val exception = Exception("Failed to parse the EPCIS document from the XML. xml=$strValue", ex)
            OTLogger.Error(exception)
            throw exception
        }
    }

    fun Map(doc: EPCISQueryDocument): String {
        if (doc.EpcisVersion != EPCISVersion.V2) {
            throw Exception("doc.epcisVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.")
        }
        val epcisNS = if (doc.EpcisVersion == EPCISVersion.V2) Constants.EPCISQUERY_2_NAMESPACE else Constants.EPCISQUERY_1_NAMESPACE
        val namespacesReversed = doc.Namespaces.reversed()
        val jEventsList = JArray()
        for (e in doc.Events) {
            val jEvent = OpenTraceabilityJsonLDMapper.ToJson(e, namespacesReversed) as JObject?
            if (jEvent != null) {
                jEventsList.add(jEvent)
            }
        }
        val json = EPCISDocumentBaseJsonMapper.writeJson(doc, epcisNS, "EPCISQueryDocument")
        val jEPCISBody = JObject()
        val jQueryResults = JObject()
        val jResultsBody = JObject()
        jQueryResults["queryName"] = doc.QueryName
        jQueryResults["subscriptionID"] = doc.SubscriptionID
        jResultsBody["eventList"] = jEventsList
        jQueryResults["resultsBody"] = jResultsBody
        jEPCISBody["queryResults"] = jQueryResults
        json["epcisBody"] = jEPCISBody
        EPCISDocumentBaseJsonMapper.conformEPCISJsonLD(json, doc.Namespaces)
        EPCISDocumentBaseJsonMapper.checkSchema(json)
        return json.toString()
    }
}
