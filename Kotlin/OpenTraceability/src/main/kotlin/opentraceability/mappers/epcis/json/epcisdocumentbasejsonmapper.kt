package opentraceability.mappers.epcis.json

import opentraceability.models.events.*
import org.json.*
import opentraceability.utility.JsonContextHelper
import opentraceability.utility.StringExtensions.tryConvertToDateTimeOffset
import java.util.*
import com.google.gson.*
import opentraceability.interfaces.IEvent
import opentraceability.models.common.*
import opentraceability.utility.JsonContextHelper.Companion.getJsonLDContext
import opentraceability.utility.JsonContextHelper.Companion.scrapeNamespaces
import java.net.URL
import org.everit.json.schema.loader.SchemaLoader
import opentraceability.utility.JsonSchemaChecker
import opentraceability.utility.OpenTraceabilitySchemaException
import kotlin.reflect.KClass

object EPCISDocumentBaseJsonMapper {
    inline fun <reified T : EPCISBaseDocument> readJSON(strValue: String, checkSchema: Boolean = true): Pair<T, JSONObject> {
        if (checkSchema) {
            var obj = JSONObject(strValue)
            checkSchema(obj) // assuming you have a checkSchema method
        }

        var normalizedStrValue = normalizeEPCISJsonLD(strValue) // assuming you have a normalizeEPCISJsonLD method

        val settings = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create()
        val json = settings.fromJson(normalizedStrValue, JsonObject::class.java)
            ?: throw Exception("Failed to parse json from string. $strValue")

        val document = T::class.java.getDeclaredConstructor().newInstance()

        document.attributes["schemaVersion"] = json.get("schemaVersion")?.asString ?: ""
        document.epcisVersion = EPCISVersion.V2

        val creationDateAttributeStr = json.get("creationDate")?.asString
        if (!creationDateAttributeStr.isNullOrBlank()) {
            document.creationDate = creationDateAttributeStr.tryConvertToDateTimeOffset() // assuming you have a tryConvertToDateTimeOffset method
        }

        document.attributes = HashMap()

        val jContextArray = json.getAsJsonArray("@context")
        if (jContextArray != null) {
            for (i in 0 until jContextArray.count()) {
                val jt = jContextArray.get(i)
                if (jt is JsonObject) {
                    val ns = scrapeNamespaces( JSONObject(jt.toString())) // assuming you have a scrapeNamespaces method
                    ns.forEach { (key, value) ->
                        document.namespaces.putIfAbsent(key, value)
                    }
                    document.contexts.add(jt.toString())
                } else {
                    val value = jt.toString()
                    if (!value.isBlank()) {
                        var value = value.replace("\"","")

                        val context = getJsonLDContext(value) // assuming you have a getJsonLDContext method
                        val ns = scrapeNamespaces(context)
                        ns.forEach { (key, value) ->
                            document.namespaces.putIfAbsent(key, value)
                        }
                        document.contexts.add(value)
                    }
                }
            }
        } else {
            throw Exception("the @context on the root of the JSON-LD EPCIS file was not an array. we are currently expecting this to be an array.")
        }

        json.get("id")?.asString?.let {
            document.attributes["id"] = it
        }

        document.header = StandardBusinessDocumentHeader() // assuming you have a StandardBusinessDocumentHeader class

        document.header?.Sender = SBDHOrganization() // assuming you have a SBDHOrganization class
        document.header?.Sender?.Identifier = json.get("sender")?.asString

        document.header?.Receiver = SBDHOrganization()
        document.header?.Receiver?.Identifier = json.get("receiver")?.asString

        document.header?.DocumentIdentification = SBDHDocumentIdentification() // assuming you have a SBDHDocumentIdentification class
        document.header?.DocumentIdentification?.InstanceIdentifier = json.get("instanceIdentifier")?.asString

        return Pair(document, JSONObject(json.toString()))
    }

    fun writeJson(doc: EPCISBaseDocument, epcisNS: String, docType: String): JSONObject {
        if (doc.epcisVersion != EPCISVersion.V2) {
            throw Exception("doc.epcisVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.")
        }

        // construct the main JSON object
        val jobj = JSONObject()
        jobj.put("@context", doc.contexts)
        jobj.put("@type", docType)


        // add all of the core attributes of the EPCIS document...
        jobj.put("schemaVersion", doc.attributes["schemaVersion"])
        jobj.put("epcisVersion", "2.0")
        jobj.put("id", doc.attributes["id"])
        jobj.put("creationDate", doc.creationDate)

        jobj.put("sender", doc.header?.Sender?.Identifier)
        jobj.put("receiver", doc.header?.Receiver?.Identifier)
        jobj.put("instanceIdentifier", doc.header?.DocumentIdentification?.InstanceIdentifier)
        return jobj
    }

    fun getEventTypeFromProfile(jEvent: JSONObject): KClass<*> {
        val action: EventAction? = EventAction.values().find { it.name == jEvent.optString("action") }
        val bizStep: String? = jEvent.optString("bizStep")
        val eventType: String = jEvent.optString("type") ?: throw Exception("type property not set on event ${jEvent.toString()}")

        val profiles = opentraceability.Setup.Profiles
            .filter { it.EventType.toString() == eventType && (it.Action == null || it.Action == action) && (it.BusinessStep == null || it.BusinessStep.equals(
                bizStep, ignoreCase = true)) }
            .sortedByDescending { it.SpecificityScore }
            .toMutableList()

        if (profiles.isEmpty()) {
            throw Exception("Failed to create event from profile. Type=$eventType and BizStep=$bizStep and Action=$action")
        } else {
            profiles.filter { it.KDEProfiles != null }.forEach { profile ->
                profile.KDEProfiles?.forEach { kdeProfile ->
                    if (jEvent.opt(kdeProfile.JPath) == null) {
                        profiles.remove(profile)
                    }
                }
            }

            if (profiles.isEmpty()) {
                throw Exception("Failed to create event from profile. Type=$eventType and BizStep=$bizStep and Action=$action")
            }

            return profiles.first().EventClassType
        }
    }

    fun checkSchema(json: JSONObject) {
        val jsonString = json.toString()
        val schemaUrl = "https://ref.gs1.org/standards/epcis/epcis-json-schema.json"

        val (isValid, errors) = JsonSchemaChecker.isValid(jsonString, schemaUrl)

        if (!isValid) {
            val errorString = errors.joinToString("\n")
            throw OpenTraceabilitySchemaException("Failed to validate JSON schema with errors:\n$errorString\n\n and json $jsonString")
        }
    }


    internal fun getEventType(e: IEvent): String {
        return when (e.eventType) {
            EventType.ObjectEvent -> "ObjectEvent"
            EventType.TransformationEvent -> "TransformationEvent"
            EventType.TransactionEvent -> "TransactionEvent"
            EventType.AggregationEvent -> "AggregationEvent"
            EventType.AssociationEvent -> "AssociationEvent"
            else -> throw Exception("Failed to determine the event type. Event Kotlin type is ${e::class.qualifiedName}")
        }
    }

    fun conformEPCISJsonLD(json: JSONObject, namespaces: MutableMap<String, String>) {
        compressVocab(json)
    }

    fun compressVocab(json: Any): Any {
        if (json is JSONObject) {
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = json.opt(key)
                when (value) {
                    is JSONObject -> json.put(key, compressVocab(value))
                    is JSONArray -> {
                        for (i in 0 until value.length()) {
                            value.put(i, compressVocab(value[i]))
                        }
                    }
                    else -> json.put(key, compressVocab(value))
                }
            }
            return json
        } else {
            val str = json.toString()
            val newVal = when {
                str.startsWith("urn:epcglobal:cbv:btt:") ||
                        str.startsWith("urn:epcglobal:cbv:bizstep:") ||
                        str.startsWith("urn:epcglobal:cbv:sdt:") ||
                        str.startsWith("urn:epcglobal:cbv:disp:") -> {
                    str.split(":").last()
                }
                str.startsWith("https://ref.gs1.org/cbv") -> {
                    str.split("-").last()
                }
                str.startsWith("https://gs1.org/voc/") -> {
                    str.split("/").last()
                }
                else -> str
            }
            return JSONTokener(newVal).nextValue()
        }
    }

    fun normalizeEPCISJsonLD(jEPCISStr: String): String {
        val gson: Gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create()
        val json = gson.fromJson(jEPCISStr, JsonObject::class.java) ?: throw Exception("Failed to parse json from string. $jEPCISStr")

        val jEPCISContext = JsonContextHelper.getJsonLDContext("https://ref.gs1.org/standards/epcis/epcis-context.jsonld")
        val namespaces = JsonContextHelper.scrapeNamespaces(jEPCISContext)

        var jEventList: JsonArray? = json.getAsJsonObject("epcisBody")?.getAsJsonArray("eventList")
        if (jEventList == null) {
            jEventList = json.getAsJsonObject("epcisBody")?.getAsJsonObject("queryResults")?.getAsJsonObject("resultsBody")?.getAsJsonArray("eventList")
        }
        jEventList?.let {
            for (i in 0 until it.count()) {
                val jEvent = it.get(i)
                jEvent?.let { JsonContextHelper.expandVocab(it, jEPCISContext, namespaces) }
            }
        }

        return json.toString()
    }
}

