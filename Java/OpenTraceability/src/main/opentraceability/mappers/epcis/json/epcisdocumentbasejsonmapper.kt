package mappers.epcis.json

import com.fasterxml.jackson.core.JsonToken
import com.intellij.json.psi.JsonObject
import interfaces.IEvent
import models.events.*
import models.events.EPCISBaseDocument
import models.events.EventType
import java.lang.reflect.Type

class EPCISDocumentBaseJsonMapper {
    companion object {
        inline fun <reified T : Any> ReadJSON(strValue: String, json: JsonObject, checkSchema: Boolean = true): T? {
            TODO("Not yet implemented")
        }

        fun WriteJson(doc: EPCISBaseDocument, epcisNS: String, docType: String): JsonObject {
            TODO("Not yet implemented")
        }


        internal fun GetEventTypeFromProfile(jEvent: JsonObject): Type {
            TODO("Not yet implemented")
        }

        internal fun CheckSchema(json: JsonObject) {
            TODO("Not yet implemented")

        }

        internal fun GetEventType(e: IEvent): String {

            TODO("Is this error due to the parameter being an interface?")

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

        internal fun ConformEPCISJsonLD(json: JsonObject, namespaces: MutableMap<String, String>) {
            TODO("Not yet implemented")
        }

        internal fun CompressVocab(json: JsonToken): JsonToken {
            TODO("Not yet implemented")
        }

        internal fun NormalizeEPCISJsonLD(jEPCISStr: String): String {
            TODO("Not yet implemented")
        }
    }
}
