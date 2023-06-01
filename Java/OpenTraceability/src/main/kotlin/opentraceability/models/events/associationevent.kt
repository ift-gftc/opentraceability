package models.events

import interfaces.IEvent
import interfaces.IEventKDE
import java.util.*
import models.identifiers.*
import utility.attributes.*
import java.net.URI


@Suppress("LocalVariableName", "PropertyName")
class AssociationEvent : EventBase(), IEvent {

    @OpenTraceabilityAttribute("","parentID", 7)
    var ParentID: EPC? = null

    @OpenTraceabilityProductsAttribute("childQuantityList", EPCISVersion.V2, EventProductType.Child, 9, OpenTraceabilityProductsListType.QuantityList)
    @OpenTraceabilityProductsAttribute("childEPCs", EPCISVersion.V2, EventProductType.Child, 8, OpenTraceabilityProductsListType.EPCList)
    var Children: MutableList<EventProduct> = mutableListOf()

    @OpenTraceabilityAttribute("","action", 10)
    override  var Action: EventAction? = null

    @OpenTraceabilityAttribute("","bizStep", 11)
    override  var BusinessStep: URI? = null


    @OpenTraceabilityAttribute("","disposition", 12)
    override  var Disposition: URI? = null


    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","readPoint", 13)
    override  var ReadPoint: EventReadPoint? = EventReadPoint()


    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","bizLocation", 14)
    override var Location: EventLocation? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("bizTransaction")
    @OpenTraceabilityAttribute("","bizTransactionList", 15, EPCISVersion.V2)
    override  var BizTransactionList: MutableList<EventBusinessTransaction> = mutableListOf()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("source")
    @OpenTraceabilityAttribute("","sourceList", 16, EPCISVersion.V2)
    override var SourceList: MutableList<EventSource> = mutableListOf()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("destination")
    @OpenTraceabilityAttribute("","destinationList", 17, EPCISVersion.V2)
    override var DestinationList: MutableList<EventDestination> = mutableListOf()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("sensorElement")
    @OpenTraceabilityAttribute("","sensorElementList", 18, EPCISVersion.V2)
    override var SensorElementList: MutableList<SensorElement> = mutableListOf()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","persistentDisposition", 19)
    override var PersistentDisposition: PersistentDisposition? = PersistentDisposition()

    var ILMD: EventILMD = EventILMD()

    @OpenTraceabilityXmlIgnoreAttribute
    @OpenTraceabilityAttribute("","type", 0)
    override lateinit var EventType: EventType


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
                throw Exception("Association event only supports children and parents.")
            }
        }
    }

    override fun GetILMD(): EventILMD? {
        return ILMD
    }


}
