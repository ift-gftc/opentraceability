package opentraceability.models.events

import com.fasterxml.jackson.annotation.*
import opentraceability.interfaces.*
import opentraceability.models.identifiers.EPC
import opentraceability.utility.attributes.*
import java.net.URI
import kotlin.collections.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Suppress("LocalVariableName", "PropertyName")
class AggregationEvent<T : EventILMD>(
    @JsonProperty("@type")
    val type: EventType = EventType.TransformationEvent,

    @OpenTraceabilityAttribute("", "parentID", 7)
    override var parentID: EPC? = null,

    @OpenTraceabilityProductsAttribute(
        "extension/childQuantityList",
        EPCISVersion.V1,
        EventProductType.Child,
        21,
        OpenTraceabilityProductsListType.QuantityList
    )
    @OpenTraceabilityProductsAttribute("childQuantityList", EPCISVersion.V2, EventProductType.Child, 15, OpenTraceabilityProductsListType.QuantityList)
    @OpenTraceabilityProductsAttribute("childEPCs", EPCISVersion.V2, EventProductType.Child, 8, OpenTraceabilityProductsListType.EPCList,  true)
    var children: MutableList<EventProduct> = mutableListOf(),

    @OpenTraceabilityAttribute("", "action", 9)
    override var action: EventAction? = null,

    @OpenTraceabilityAttribute("", "bizStep", 10)
    override var businessStep: URI? = null,

    @OpenTraceabilityAttribute("", "disposition", 11)
    override var disposition: URI? = null,

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "readPoint", 12)
    override var readPoint: EventReadPoint? = null,

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "bizLocation", 13)
    override var location: EventLocation? = null,

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("bizTransaction")
    @OpenTraceabilityAttribute("", "bizTransactionList", 14)
    override var bizTransactionList: MutableList<EventBusinessTransaction> = mutableListOf(),

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("source")
    @OpenTraceabilityAttribute("", "sourceList", 16, EPCISVersion.V2)
    @OpenTraceabilityAttribute("","baseExtension/sourceList", 22, EPCISVersion.V1)
    override var sourceList: MutableList<EventSource> = mutableListOf(),

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("destination")
    @OpenTraceabilityAttribute("", "destinationList", 17, EPCISVersion.V2)
    @OpenTraceabilityAttribute("","baseExtension/destinationList", 23, EPCISVersion.V1)
    override var destinationList: MutableList<EventDestination> = mutableListOf(),

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("sensorElement")
    @OpenTraceabilityAttribute("", "sensorElementList", 18, EPCISVersion.V2)
    override var sensorElementList: MutableList<SensorElement> = mutableListOf(),

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "persistentDisposition", 19, EPCISVersion.V2)
    override var persistentDisposition: PersistentDisposition? = null,

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "ilmd", 20, EPCISVersion.V2)
    override var ilmd: T? = null
) : EventBase(), IAggregationEvent, IILMDEvent<T> {

    @OpenTraceabilityXmlIgnoreAttribute
    @OpenTraceabilityAttribute("", "type", 0)
    override var eventType: EventType = opentraceability.models.events.EventType.AggregationEvent

    override var products: MutableList<EventProduct> = mutableListOf()
        get() {
            val prods: MutableList<EventProduct> = mutableListOf()
            this.parentID?.let {
                prods.add(EventProduct(it).apply { Type = EventProductType.Parent })
            }
            prods.addAll(this.children)
            return prods
        }

    override fun <T: IEventKDE> getKDE(clazz: Class<T>, ns: String, name: String): T? {
        return super.getKDE<T>(clazz, ns, name)
    }

    override fun <T: IEventKDE> getKDE(clazz: Class<T>): T? {
        return super.getKDE<T>(clazz)
    }

    override fun addProduct(product: EventProduct) {
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

    override fun grabILMD(): EventILMD? {
        return ilmd
    }
}