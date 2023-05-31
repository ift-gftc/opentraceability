package mappers.epcis.json

import models.events.*
import org.json.*
import utility.JsonContextHelper
import utility.StringExtensions.tryConvertToDateTimeOffset
import java.lang.reflect.Type
import java.util.*
import com.google.gson.*
import models.common.*
import java.net.URL
import org.everit.json.schema.loader.SchemaLoader
import utility.JsonContextHelper.getJsonLDContext
import utility.JsonContextHelper.scrapeNamespaces

object EPCISDocumentBaseJsonMapper {



    inline fun <reified T : EPCISBaseDocument> readJSON(strValue: String, checkSchema: Boolean = true): Pair<T, JSONObject> {
        if (checkSchema) {
            checkSchema(JSONObject(strValue)) // assuming you have a checkSchema method
        }

        var normalizedStrValue = normalizeEPCISJsonLD(strValue) // assuming you have a normalizeEPCISJsonLD method

        val settings = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create()
        val json = settings.fromJson(normalizedStrValue, JSONObject::class.java)
            ?: throw Exception("Failed to parse json from string. $strValue")

        val document = T::class.java.getDeclaredConstructor().newInstance()

        document.Attributes["schemaVersion"] = json.optString("schemaVersion", "")
        document.EPCISVersion = EPCISVersion.V2

        val creationDateAttributeStr = json.optString("creationDate")
        if (!creationDateAttributeStr.isNullOrBlank()) {
            document.CreationDate = creationDateAttributeStr.tryConvertToDateTimeOffset() // assuming you have a tryConvertToDateTimeOffset method
        }

        document.Attributes = HashMap()

        val jContextArray = json.optJSONArray("@context")
        if (jContextArray != null) {
            for (i in 0 until jContextArray.length()) {
                val jt = jContextArray.get(i)
                if (jt is JSONObject) {
                    val ns = scrapeNamespaces(jt) // assuming you have a scrapeNamespaces method
                    ns.forEach { (key, value) ->
                        document.Namespaces.putIfAbsent(key, value)
                    }
                    document.Contexts.add(jt.toString())
                } else {
                    val value = jt.toString()
                    if (!value.isBlank()) {
                        val context = getJsonLDContext(value) // assuming you have a getJsonLDContext method
                        val ns = scrapeNamespaces(context)
                        ns.forEach { (key, value) ->
                            document.Namespaces.putIfAbsent(key, value)
                        }
                        document.Contexts.add(value)
                    }
                }
            }
        } else {
            throw Exception("the @context on the root of the JSON-LD EPCIS file was not an array. we are currently expecting this to be an array.")
        }

        json.optString("id")?.let {
            document.Attributes["id"] = it
        }

        document.Header = StandardBusinessDocumentHeader() // assuming you have a StandardBusinessDocumentHeader class

        document.Header?.Sender = SBDHOrganization() // assuming you have a SBDHOrganization class
        document.Header?.Sender?.Identifier = json.optString("sender")

        document.Header?.Receiver = SBDHOrganization()
        document.Header?.Receiver?.Identifier = json.optString("receiver")

        document.Header?.DocumentIdentification = SBDHDocumentIdentification() // assuming you have a SBDHDocumentIdentification class
        document.Header?.DocumentIdentification?.InstanceIdentifier = json.optString("instanceIdentifier")

        return Pair(document, json)
    }





    fun writeJson(doc: EPCISBaseDocument, epcisNS: String, docType: String): JSONObject {
        if (doc.EPCISVersion != EPCISVersion.V2) {
            throw Exception("doc.EPCISVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.")
        }

        // construct the main JSON object
        val jobj = JSONObject()
        jobj.put("@context", doc.Contexts)
        jobj.put("@type", docType)


        // add all of the core attributes of the EPCIS document...
        jobj.put("schemaVersion", doc.Attributes["schemaVersion"])
        jobj.put("epcisVersion", "2.0")
        jobj.put("id", doc.Attributes["id"])
        jobj.put("creationDate", doc.CreationDate)

        jobj.put("sender", doc.Header?.Sender?.Identifier)
        jobj.put("receiver", doc.Header?.Receiver?.Identifier)
        jobj.put("instanceIdentifier", doc.Header?.DocumentIdentification?.InstanceIdentifier)
        return jobj
    }

    fun checkSchema(json: JSONObject) {
        val schemaUrl = URL("https://ref.gs1.org/standards/epcis/epcis-json-schema.json")
        val schemaJson = JSONObject(JSONTokener(schemaUrl.openStream()))
        val schema = SchemaLoader.load(schemaJson)
        try {
            schema.validate(json)  // throws a ValidationException if this object is invalid
        } catch (e: Exception) {
            throw Exception("Failed to validate JSON schema with errors:\n" + e.message + "\n\n and json " + json.toString(2))
        }
    }



    fun normalizeEPCISJsonLD(jEPCISStr: String): String {
        val gson: Gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create()
        val json = gson.fromJson(jEPCISStr, JSONObject::class.java) ?: throw Exception("Failed to parse json from string. $jEPCISStr")

        val jEPCISContext = JsonContextHelper.getJsonLDContext("https://ref.gs1.org/standards/epcis/epcis-context.jsonld")
        val namespaces = JsonContextHelper.scrapeNamespaces(jEPCISContext)

        var jEventList: JSONArray? = json.optJSONObject("epcisBody")?.optJSONArray("eventList")
        if (jEventList == null) {
            jEventList = json.optJSONObject("epcisBody")?.optJSONObject("queryResults")?.optJSONObject("resultsBody")?.optJSONArray("eventList")
        }
        jEventList?.let {
            for (i in 0 until it.length()) {
                val jEvent = it.optJSONObject(i)
                jEvent?.let { JsonContextHelper.expandVocab(it, jEPCISContext, namespaces) }
            }
        }

        return json.toString()
    }


    fun getEventTypeFromProfile(jEvent: JSONObject): Type {
        val action: EventAction? = EventAction.values().find { it.name == jEvent.optString("action") }
        val bizStep: String? = jEvent.optString("bizStep")
        val eventType: String = jEvent.optString("type") ?: throw Exception("type property not set on event ${jEvent.toString()}")

        val profiles = Setup.Profiles
            .filter { it.EventType.toString() == eventType && (it.Action == null || it.Action == action) && (it.BusinessStep == null || it.BusinessStep?.toLowerCase() == bizStep?.toLowerCase()) }
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


    fun conformEPCISJsonLD(json: JSONObject, namespaces: Map<String, String>) {
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
}

