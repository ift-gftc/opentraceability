package models.events

import interfaces.IILMDEvent
import java.util.*
import utility.attributes.*
import java.net.URI

class ObjectEvent<T> : EventBase(), IILMDEvent<T> {

    @OpenTraceabilityXmlIgnoreAttribute
    @OpenTraceabilityAttribute("", "type", 0)
    override  var EventType: EventType = models.events.EventType.ObjectEvent

    @OpenTraceabilityProductsAttribute("extension/quantityList",EPCISVersion.V1,EventProductType.Reference,20,OpenTraceabilityProductsListType.QuantityList)
    @OpenTraceabilityProductsAttribute("quantityList",EPCISVersion.V2,EventProductType.Reference,14,OpenTraceabilityProductsListType.QuantityList)
    @OpenTraceabilityProductsAttribute("epcList",EPCISVersion.V2,EventProductType.Reference,7,OpenTraceabilityProductsListType.EPCList,true)
    var ReferenceProducts: ArrayList<EventProduct> = ArrayList<EventProduct>()

    @OpenTraceabilityAttribute("", "action", 8)
    override  var Action: EventAction? = null

    @OpenTraceabilityAttribute("", "bizStep", 9)
    override  var BusinessStep: URI? = null

    @OpenTraceabilityAttribute("", "disposition", 10)
    override   var Disposition: URI? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "readPoint", 11)
    override  var ReadPoint: EventReadPoint? = null


    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "bizLocation", 12)
    override  var Location: EventLocation? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("bizTransaction")
    @OpenTraceabilityAttribute("", "bizTransactionList", 13)
    override   var BizTransactionList: MutableList<EventBusinessTransaction> = mutableListOf()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("source")
    @OpenTraceabilityAttribute("", "sourceList", 15, EPCISVersion.V2)
    @OpenTraceabilityAttribute("", "extension/sourceList", 21, EPCISVersion.V1)
    override  var SourceList: MutableList<EventSource> = mutableListOf()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("destination")
    @OpenTraceabilityAttribute("", "destinationList", 16, EPCISVersion.V2)
    @OpenTraceabilityAttribute("", "extension/destinationList", 22, EPCISVersion.V1)
    override   var DestinationList: MutableList<EventDestination> = mutableListOf()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("sensorElement")
    @OpenTraceabilityAttribute("", "sensorElementList", 17, EPCISVersion.V2)
    @OpenTraceabilityAttribute("", "extension/sensorElementList", 17, EPCISVersion.V1)
    override   var SensorElementList: MutableList<SensorElement> = mutableListOf()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "persistentDisposition", 18)
    @OpenTraceabilityAttribute("", "extension/persistentDisposition", 18, EPCISVersion.V1)
    override   var PersistentDisposition: PersistentDisposition? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "ilmd", 19, EPCISVersion.V2)
    @OpenTraceabilityAttribute("", "extension/ilmd", 23, EPCISVersion.V1)
    override  var ILMD: T? = null

    override  var Products: MutableList<EventProduct> = mutableListOf()
        get() {
            val products: MutableList<EventProduct> = mutableListOf()
            products.addAll(ReferenceProducts)
            return products
        }

    override   fun AddProduct(product: EventProduct) {
        if (product.Type == EventProductType.Reference) {
            ReferenceProducts.add(product)
        } else {
            throw Exception("Object event only supports references.")
        }
    }

    override fun GetILMD(): EventILMD? {
        return ILMD as EventILMD
    }

}
