package models.events

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeInfo
import interfaces.IAggregationEvent
import interfaces.IILMDEvent
import models.identifiers.EPC
import utility.attributes.*
import utility.attributes.OpenTraceabilityXmlIgnoreAttribute
import utility.attributes.*
import java.net.URI
import kotlin.collections.ArrayList
import kotlin.collections.List

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class AggregationEvent<T : EventILMD>(
    @JsonProperty("@type")
    val type: EventType = EventType.TransformationEvent,

    @OpenTraceabilityAttribute("", "parentID", 7)
    var parentID: EPC? = null,

    @OpenTraceabilityProductsAttribute(
        "extension/childQuantityList",
        EPCISVersion.V1,
        EventProductType.Child,
        21,
        OpenTraceabilityProductsListType.QuantityList
    )
    //@OpenTraceabilityProductsAttribute("childQuantityList", EPCISVersion.V2, EventProductType.CHILD, 15, OpenTraceabilityProductsListType.QuantityList)
    //@OpenTraceabilityProductsAttribute("childEPCs", EventProductType.CHILD, 8, OpenTraceabilityProductsListType.EPC_LIST, required = true)
    var children: MutableList<EventProduct> = ArrayList(),

    @OpenTraceabilityAttribute("", "action", 9)
    var action: EventAction? = null,

    @OpenTraceabilityAttribute("", "bizStep", 10)
    var businessStep: URI? = null,

    @OpenTraceabilityAttribute("", "disposition", 11)
    var disposition: URI? = null,

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "readPoint", 12)
    var readPoint: EventReadPoint? = null,

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "bizLocation", 13)
    var location: EventLocation? = null,

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("bizTransaction")
    @OpenTraceabilityAttribute("", "bizTransactionList", 14)
    var bizTransactionList: MutableList<EventBusinessTransaction> = ArrayList(),

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("source")
    @OpenTraceabilityAttribute("", "sourceList", 16, EPCISVersion.V2)
    //@OpenTraceabilityAttribute("","baseExtension/sourceList", 22, EPCISVersion.V1)
    var sourceList: MutableList<EventSource> = ArrayList(),

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("destination")
    @OpenTraceabilityAttribute("", "destinationList", 17, EPCISVersion.V2)
    //@OpenTraceabilityAttribute("","baseExtension/destinationList", 23, EPCISVersion.V1)
    var destinationList: MutableList<EventDestination> = ArrayList(),

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("sensorElement")
    @OpenTraceabilityAttribute("", "sensorElementList", 18, EPCISVersion.V2)
    var sensorElementList: MutableList<SensorElement> = ArrayList(),

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "persistentDisposition", 19, EPCISVersion.V2)
    var persistentDisposition: PersistentDisposition? = null,

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "ilmd", 20, EPCISVersion.V2)
    var ilmd: T? = null
) : EventBase(), IAggregationEvent, IILMDEvent<T> {

    @OpenTraceabilityXmlIgnoreAttribute
    @OpenTraceabilityAttribute("", "type", 0)
    val eventType: EventType
        get() {
            return EventType.AggregationEvent
        }

    val products: List<EventProduct>
        get() {
            val products = ArrayList<EventProduct>()
            this.parentID?.let {
                products.add(EventProduct(it).apply { Type = EventProductType.Parent })
            }
            products.addAll(this.children)
            return products
        }

    fun addProduct(product: EventProduct) {
        when (product.Type) {
            EventProductType.Parent -> {
                if (product.Quantity != null) {
                    throw Exception("Parents do not support quantity.")
                }
                this.parentID = product.EPC
            }

            EventProductType.Child -> {
                this.children.add(product)
            }

            else -> {
                throw Exception("Aggregation event only supports children and parents.")
            }
        }
    }
}