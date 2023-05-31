package models.events

import java.util.*
import utility.attributes.*
import java.net.URI

class ObjectEvent<T> : EventBase() {

    @OpenTraceabilityXmlIgnoreAttribute
    @OpenTraceabilityAttribute("", "type", 0)
    lateinit var EventType: EventType

    @OpenTraceabilityProductsAttribute(
        "extension/quantityList",
        EPCISVersion.V1,
        EventProductType.Reference,
        20,
        OpenTraceabilityProductsListType.QuantityList
    )
    @OpenTraceabilityProductsAttribute(
        "quantityList",
        EPCISVersion.V2,
        EventProductType.Reference,
        14,
        OpenTraceabilityProductsListType.QuantityList
    )
    @OpenTraceabilityProductsAttribute("epcList",EPCISVersion.V2,EventProductType.Reference,7,OpenTraceabilityProductsListType.EPCList,true)
    var ReferenceProducts: ArrayList<EventProduct> = ArrayList<EventProduct>()

    @OpenTraceabilityAttribute("", "action", 8)
    var Action: EventAction? = null

    @OpenTraceabilityAttribute("", "bizStep", 9)
    var BusinessStep: URI? = null

    @OpenTraceabilityAttribute("", "disposition", 10)
    var Disposition: URI? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "readPoint", 11)
    var ReadPoint: EventReadPoint? = null


    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "bizLocation", 12)
    var Location: EventLocation? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("bizTransaction")
    @OpenTraceabilityAttribute("", "bizTransactionList", 13)
    var BizTransactionList: ArrayList<EventBusinessTransaction> = ArrayList<EventBusinessTransaction>()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("source")
    @OpenTraceabilityAttribute("", "sourceList", 15, EPCISVersion.V2)
    @OpenTraceabilityAttribute("", "extension/sourceList", 21, EPCISVersion.V1)
    var SourceList: ArrayList<EventSource> = ArrayList<EventSource>()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("destination")
    @OpenTraceabilityAttribute("", "destinationList", 16, EPCISVersion.V2)
    @OpenTraceabilityAttribute("", "extension/destinationList", 22, EPCISVersion.V1)
    var DestinationList: ArrayList<EventDestination> = ArrayList<EventDestination>()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("sensorElement")
    @OpenTraceabilityAttribute("", "sensorElementList", 17, EPCISVersion.V2)
    @OpenTraceabilityAttribute("", "extension/sensorElementList", 17, EPCISVersion.V1)
    var SensorElementList: ArrayList<SensorElement> = ArrayList<SensorElement>()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "persistentDisposition", 18)
    @OpenTraceabilityAttribute("", "extension/persistentDisposition", 18, EPCISVersion.V1)
    var PersistentDisposition: PersistentDisposition? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "ilmd", 19, EPCISVersion.V2)
    @OpenTraceabilityAttribute("", "extension/ilmd", 23, EPCISVersion.V1)
    var ILMD: T? = null

    var Products: ArrayList<EventProduct> = ArrayList<EventProduct>()

    val products: List<EventProduct>
        get() {
            val products = ArrayList<EventProduct>()
            products.addAll(ReferenceProducts)
            return products
        }

    fun addProduct(product: EventProduct) {
        if (product.Type == EventProductType.Reference) {
            ReferenceProducts.add(product)
        } else {
            throw Exception("Object event only supports references.")
        }
    }

}
