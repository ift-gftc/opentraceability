package opentraceability.models.events

import opentraceability.interfaces.IEvent
import opentraceability.interfaces.IEventKDE
import opentraceability.models.identifiers.*
import opentraceability.utility.attributes.*
import java.util.*
import java.net.URI


@Suppress("LocalVariableName", "PropertyName")
class AssociationEvent : EventBase(), IEvent {

    @OpenTraceabilityAttribute("","parentID", 7)
    var parentID: EPC? = null

    @OpenTraceabilityProductsAttribute("childQuantityList", EPCISVersion.V2, EventProductType.Child, 9, OpenTraceabilityProductsListType.QuantityList)
    @OpenTraceabilityProductsAttribute("childEPCs", EPCISVersion.V2, EventProductType.Child, 8, OpenTraceabilityProductsListType.EPCList)
    var children: MutableList<EventProduct> = mutableListOf()

    @OpenTraceabilityAttribute("","action", 10)
    override var action: EventAction? = null

    @OpenTraceabilityAttribute("","bizStep", 11)
    override var businessStep: URI? = null

    @OpenTraceabilityAttribute("","disposition", 12)
    override var disposition: URI? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","readPoint", 13)
    override var readPoint: EventReadPoint? = EventReadPoint()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","bizLocation", 14)
    override var location: EventLocation? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("bizTransaction")
    @OpenTraceabilityAttribute("","bizTransactionList", 15, EPCISVersion.V2)
    override var bizTransactionList: MutableList<EventBusinessTransaction> = mutableListOf()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("source")
    @OpenTraceabilityAttribute("","sourceList", 16, EPCISVersion.V2)
    override var sourceList: MutableList<EventSource> = mutableListOf()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("destination")
    @OpenTraceabilityAttribute("","destinationList", 17, EPCISVersion.V2)
    override var destinationList: MutableList<EventDestination> = mutableListOf()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("sensorElement")
    @OpenTraceabilityAttribute("","sensorElementList", 18, EPCISVersion.V2)
    override var sensorElementList: MutableList<SensorElement> = mutableListOf()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","persistentDisposition", 19)
    override var persistentDisposition: PersistentDisposition? = PersistentDisposition()

    var ilmd: EventILMD = EventILMD()

    @OpenTraceabilityXmlIgnoreAttribute
    @OpenTraceabilityAttribute("","type", 0)
    override lateinit var eventType: EventType


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
                throw Exception("Association event only supports children and parents.")
            }
        }
    }

    override fun grabILMD(): EventILMD? {
        return ilmd
    }


}
