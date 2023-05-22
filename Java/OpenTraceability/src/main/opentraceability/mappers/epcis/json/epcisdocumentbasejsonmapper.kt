package mappers.epcis.json

import com.fasterxml.jackson.core.JsonToken
import interfaces.IEvent
import models.events.*
import org.json.simple.JSONObject
import java.lang.reflect.Type
import java.util.*
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.typeOf

class EPCISDocumentBaseJsonMapper {
    companion object {
    }

    inline fun <reified T : Any> ReadJSON(strValue: String, json: JSONObject, checkSchema: Boolean = true): T? {

        TODO("Not yet implemented")
        return T::class.primaryConstructor!!.call()
    }

    fun WriteJson(doc: EPCISBaseDocument, epcisNS: String, docType: String): JSONObject {

        TODO("Not yet implemented")
        return JSONObject()
    }


    internal fun GetEventTypeFromProfile(jEvent: JSONObject): KType {


        TODO("Not yet implemented")
        return typeOf<String>()
    }

    internal fun CheckSchema(json: JSONObject) {
        TODO("Not yet implemented")

    }

    internal fun GetEventType(e: IEvent): String {
        if (e.EventType == EventType.ObjectEvent) {
            return "ObjectEvent";
        } else if (e.EventType == EventType.TransformationEvent) {
            return "TransformationEvent";
        } else if (e.EventType == EventType.TransactionEvent) {
            return "TransactionEvent";
        } else if (e.EventType == EventType.AggregationEvent) {
            return "AggregationEvent";
        } else if (e.EventType == EventType.AssociationEvent) {
            return "AssociationEvent";
        } else {
            throw Exception("Failed to determine the event type. Event C# type is " + e::class.simpleName);
        }
    }

    internal fun ConformEPCISJsonLD(json: JSONObject, namespaces: Dictionary<String, String>) {

        TODO("Not yet implemented")
        //CompressVocab(json)
    }

    internal fun CompressVocab(json: JsonToken): JsonToken {
        TODO("Not yet implemented")
        return json;
    }

    internal fun NormalizeEPCISJsonLD(jEPCISStr: String): String {
        TODO("Not yet implemented")
        return "";
    }

}
