package mappers.epcis.json

import models.events.EPCISBaseDocument
import models.events.EPCISVersion
import org.json.JSONArray
import org.json.JSONObject
import org.json.simple.JSONObject
import java.net.URI
import java.util.*

object EPCISDocumentBaseJsonMapper {

    @Throws(Exception::class)
    fun <T> ReadJSON(strValue: String, json: JSONObject, checkSchema: Boolean = true): T where T : EPCISBaseDocument, T : Any {
        // validate the JSON...
        if (checkSchema) {
            CheckSchema(JSONObject(strValue))
        }

        // normalize the json-ld
        var strValue = normalizeEPCISJsonLD(strValue)

        json = JSONObject(strValue)

        // read all of the attributes
        val document = T::class.java.getDeclaredConstructor().newInstance()

        document.attributes["schemaVersion"] = json.optString("schemaVersion", "")
        document.epcisVersion = EPCISVersion.V2

        // read the creation date
        val creationDateAttributeStr = json.optString("creationDate", null)
        if (creationDateAttributeStr != null && !creationDateAttributeStr.isEmpty()) {
            document.creationDate = creationDateAttributeStr.tryConvertToDateTimeOffset()
        }

        // read the content...
        document.attributes = HashMap()

        // we are going to break down the content into either namespaces, or links to contexts...
        val jContextArray = json.optJSONArray("@context")
        if (jContextArray != null) {
            for (jt in jContextArray) {
                // go through each item in the array...
                if (jt is JSONObject) {
                    // grab all namespaces from the jobject
                    val jobj = jt as JSONObject
                    val ns = JsonContextHelper.scrapeNamespaces(jobj)
                    for (n in ns) {
                        if (!document.namespaces.containsKey(n.key)) {
                            document.namespaces[n.key] = n.value
                        }
                    }

                    // add it to the contexts..
                    document.contexts.add(jobj.toString())
                } else {
                    val `val` = jt.toString()

                    if (`val` != null && !`val`.isEmpty()) {
                        // if this is a URL, then resolve it and grab the namespaces...
                        val jcontext = JsonContextHelper.getJsonLDContext(`val`)
                        val ns = JsonContextHelper.scrapeNamespaces(jcontext)
                        for (n in ns) {
                            if (!document.namespaces.containsKey(n.key)) {
                                document.namespaces[n.key] = n.value
                            }
                        }

                        document.contexts.add(`val`)
                    }
                }
            }
        } else throw Exception("the @context on the root of the JSON-LD EPCIS file was not an array. we are currently expecting this to be an array.")

        if (json["id"] != null) {
            document.attributes["id"] = json.optString("id", "")
        }

        // read header information
        document.header = Models.Common.StandardBusinessDocumentHeader()

        document.header.sender = Models.Common.SBDHOrganization()
        document.header.sender.identifier = json.optString("sender", "")

        document.header.receiver = Models.Common.SBDHOrganization()
        document.header.receiver.identifier = json.optString("receiver", "")

        document.header.documentIdentification = Models.Common.SBDHDocumentIdentification()
        document.header.documentIdentification.instanceIdentifier = json.optString("instanceIdentifier", "")

        return document
    }

    fun WriteJson(doc: EPCISBaseDocument, epcisNS: String, docType: String): JSONObject {
        if (doc.EpcisVersion != EPCISVersion.V2) {
            throw Exception("doc.epcisVersion is not set to V2. Only EPCIS 2.0 supports JSON-LD.")
        }

        // construct the main JSON object
        val jobj = JSONObject()
        jobj["@context"] = doc.Contexts
        jobj["@type"] = docType

        // add all of the core attributes of the EPCIS document...
        jobj["schemaVersion"] = doc.Attributes["schemaVersion"]
        jobj["epcisVersion"] = "2.0"
        jobj["id"] = doc.Attributes["id"]
        jobj["creationDate"] = doc.CreationDate

        jobj["sender"] = doc.Header?.Sender?.Identifier
        jobj["receiver"] = doc.Header?.Receiver?.Identifier
        jobj["instanceIdentifier"] = doc.Header?.DocumentIdentification?.InstanceIdentifier

        return jobj
    }

    // various helper methods...
    fun CheckSchema(jObj: JSONObject) {
        // schema checking...
    }

    fun NormalizeEPCISJsonLD(str: String): String {
        // normalizing the json-ld...
        return str
    }

    fun GetEventTypeFromProfile(profile: URI): EPCISEventType {
        // get the event type based on the profile...
        return EPCISEventType.Unknown
    }

    fun ConformEPCISJsonLD(str: String): String {
        // conform the json-ld...
        return str
    }
}

