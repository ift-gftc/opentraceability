package models.events

import com.fasterxml.jackson.annotation.*
import interfaces.*
import models.identifiers.EPC
import utility.attributes.*
import java.net.URI
import kotlin.collections.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Suppress("LocalVariableName", "PropertyName")
class AggregationEvent<T : EventILMD>(
    @JsonProperty("@type")
    val type: EventType = EventType.TransformationEvent,

    @OpenTraceabilityAttribute("", "parentID", 7)
    override var ParentID: EPC? = null,

    @OpenTraceabilityProductsAttribute(
        "extension/childQuantityList",
        EPCISVersion.V1,
        EventProductType.Child,
        21,
        OpenTraceabilityProductsListType.QuantityList
    )
    @OpenTraceabilityProductsAttribute("childQuantityList", EPCISVersion.V2, EventProductType.Child, 15, OpenTraceabilityProductsListType.QuantityList)
    @OpenTraceabilityProductsAttribute("childEPCs", EPCISVersion.V2, EventProductType.Child, 8, OpenTraceabilityProductsListType.EPCList,  true)
    var Children: MutableList<EventProduct> = mutableListOf(),

    @OpenTraceabilityAttribute("", "action", 9)
    override var Action: EventAction? = null,

    @OpenTraceabilityAttribute("", "bizStep", 10)
    override var BusinessStep: URI? = null,

    @OpenTraceabilityAttribute("", "disposition", 11)
    override var Disposition: URI? = null,

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "readPoint", 12)
    override var ReadPoint: EventReadPoint? = null,

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "bizLocation", 13)
    override var Location: EventLocation? = null,

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("bizTransaction")
    @OpenTraceabilityAttribute("", "bizTransactionList", 14)
    override var BizTransactionList: MutableList<EventBusinessTransaction> = mutableListOf(),

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("source")
    @OpenTraceabilityAttribute("", "sourceList", 16, EPCISVersion.V2)
    @OpenTraceabilityAttribute("","baseExtension/sourceList", 22, EPCISVersion.V1)
    override var SourceList: MutableList<EventSource> = mutableListOf(),

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("destination")
    @OpenTraceabilityAttribute("", "destinationList", 17, EPCISVersion.V2)
    @OpenTraceabilityAttribute("","baseExtension/destinationList", 23, EPCISVersion.V1)
    override var DestinationList: MutableList<EventDestination> = mutableListOf(),

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("sensorElement")
    @OpenTraceabilityAttribute("", "sensorElementList", 18, EPCISVersion.V2)
    override var SensorElementList: MutableList<SensorElement> = mutableListOf(),

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "persistentDisposition", 19, EPCISVersion.V2)
    override var PersistentDisposition: PersistentDisposition? = null,

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "ilmd", 20, EPCISVersion.V2)
    override var ILMD: T? = null
) : EventBase(), IAggregationEvent, IILMDEvent<T> {

    @OpenTraceabilityXmlIgnoreAttribute
    @OpenTraceabilityAttribute("", "type", 0)
    override var EventType: EventType = models.events.EventType.AggregationEvent

    override var Products: MutableList<EventProduct> = mutableListOf()
        get() {
            val products = ArrayList<EventProduct>()
            this.ParentID?.let {
                products.add(EventProduct(it).apply { Type = EventProductType.Parent })
            }
            products.addAll(this.Children)
            return products
        }

    override fun <T: IEventKDE> GetKDE(clazz: Class<T>, ns: String, name: String): T? {
        return super.GetKDE<T>(clazz, ns, name)
    }

    override fun <T: IEventKDE> GetKDE(clazz: Class<T>): T? {
        return super.GetKDE<T>(clazz)
    }

    override fun AddProduct(product: EventProduct) {
        when (product.Type) {
            EventProductType.Parent -> {
                if (product.Quantity != null) {
                    throw Exception("Parents do not support quantity.")
                }
                this.ParentID = product.EPC
            }

            EventProductType.Child -> {
                this.Children.add(product)
            }

            else -> {
                throw Exception("Aggregation event only supports children and parents.")
            }
        }
    }

    override fun GetILMD(): EventILMD? {
        return ILMD
    }
}