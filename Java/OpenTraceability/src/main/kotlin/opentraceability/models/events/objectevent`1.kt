package opentraceability.models.events

import opentraceability.interfaces.IILMDEvent
import opentraceability.utility.attributes.*
import java.util.*
import java.net.URI

class ObjectEvent<T> : EventBase(), IILMDEvent<T> {

    @OpenTraceabilityXmlIgnoreAttribute
    @OpenTraceabilityAttribute("", "type", 0)
    override  var eventType: EventType = opentraceability.models.events.EventType.ObjectEvent

    @OpenTraceabilityProductsAttribute("extension/quantityList",EPCISVersion.V1,EventProductType.Reference,20,OpenTraceabilityProductsListType.QuantityList)
    @OpenTraceabilityProductsAttribute("quantityList",EPCISVersion.V2,EventProductType.Reference,14,OpenTraceabilityProductsListType.QuantityList)
    @OpenTraceabilityProductsAttribute("epcList",EPCISVersion.V2,EventProductType.Reference,7,OpenTraceabilityProductsListType.EPCList,true)
    var referenceProducts: MutableList<EventProduct> = mutableListOf()

    @OpenTraceabilityAttribute("", "action", 8)
    override var action: EventAction? = null

    @OpenTraceabilityAttribute("", "bizStep", 9)
    override var businessStep: URI? = null

    @OpenTraceabilityAttribute("", "disposition", 10)
    override var disposition: URI? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "readPoint", 11)
    override var readPoint: EventReadPoint? = null


    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "bizLocation", 12)
    override var location: EventLocation? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("bizTransaction")
    @OpenTraceabilityAttribute("", "bizTransactionList", 13)
    override var bizTransactionList: MutableList<EventBusinessTransaction> = mutableListOf()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("source")
    @OpenTraceabilityAttribute("", "sourceList", 15, EPCISVersion.V2)
    @OpenTraceabilityAttribute("", "extension/sourceList", 21, EPCISVersion.V1)
    override var sourceList: MutableList<EventSource> = mutableListOf()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("destination")
    @OpenTraceabilityAttribute("", "destinationList", 16, EPCISVersion.V2)
    @OpenTraceabilityAttribute("", "extension/destinationList", 22, EPCISVersion.V1)
    override var destinationList: MutableList<EventDestination> = mutableListOf()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("sensorElement")
    @OpenTraceabilityAttribute("", "sensorElementList", 17, EPCISVersion.V2)
    @OpenTraceabilityAttribute("", "extension/sensorElementList", 17, EPCISVersion.V1)
    override var sensorElementList: MutableList<SensorElement> = mutableListOf()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "persistentDisposition", 18)
    @OpenTraceabilityAttribute("", "extension/persistentDisposition", 18, EPCISVersion.V1)
    override var persistentDisposition: PersistentDisposition? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "ilmd", 19, EPCISVersion.V2)
    @OpenTraceabilityAttribute("", "extension/ilmd", 23, EPCISVersion.V1)
    override var ilmd: T? = null

    override var products: MutableList<EventProduct> = mutableListOf()
        get() {
            val products: MutableList<EventProduct> = mutableListOf()
            products.addAll(referenceProducts)
            return products
        }

    override fun addProduct(product: EventProduct) {
        if (product.Type == EventProductType.Reference) {
            referenceProducts.add(product)
        } else {
            throw Exception("Object event only supports references.")
        }
    }

    override fun grabILMD(): EventILMD? {
        return ilmd as EventILMD
    }
}
