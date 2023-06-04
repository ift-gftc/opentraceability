package opentraceability.models.events

import opentraceability.interfaces.IEvent
import opentraceability.models.identifiers.EPC
import opentraceability.utility.attributes.*
import java.util.*
import java.net.URI

abstract class TransactionEvent : EventBase(), IEvent {

    @OpenTraceabilityAttribute("","parentID", 8)
    var parentID: EPC? = null

    @OpenTraceabilityProductsAttribute("extension/quantityList", EPCISVersion.V1, EventProductType.Reference, 20, OpenTraceabilityProductsListType.QuantityList)
    @OpenTraceabilityProductsAttribute("quantityList", EPCISVersion.V2, EventProductType.Reference, 15, OpenTraceabilityProductsListType.QuantityList)
    @OpenTraceabilityProductsAttribute("epcList",EPCISVersion.V2, EventProductType.Reference, 9, OpenTraceabilityProductsListType.EPCList)
    var referenceProducts: MutableList<EventProduct> =mutableListOf()

    @OpenTraceabilityAttribute("","action", 10)
    override var action: EventAction? = null

    @OpenTraceabilityAttribute("","bizStep", 11)
    override var businessStep: URI? = null

    @OpenTraceabilityAttribute("","disposition", 12)
    override var disposition: URI? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","readPoint", 13)
    override var readPoint: EventReadPoint? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","bizLocation", 14)
    override var location: EventLocation? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("bizTransaction")
    @OpenTraceabilityAttribute("","bizTransactionList", 7)
    override var bizTransactionList: MutableList<EventBusinessTransaction> = mutableListOf()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("source")
    @OpenTraceabilityAttribute("","sourceList", 16, EPCISVersion.V2)
    @OpenTraceabilityAttribute("","extension/sourceList", 21, EPCISVersion.V1)
    override var sourceList: MutableList<EventSource> = mutableListOf()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("destination")
    @OpenTraceabilityAttribute("","destinationList", 17, EPCISVersion.V2)
    @OpenTraceabilityAttribute("","extension/destinationList", 22, EPCISVersion.V1)
    override var destinationList: MutableList<EventDestination> = mutableListOf()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("sensorElement")
    @OpenTraceabilityAttribute("","sensorElementList", 18, EPCISVersion.V2)
    @OpenTraceabilityAttribute("","extension/sensorElementList",18, EPCISVersion.V1)
    override var sensorElementList: MutableList<SensorElement> = mutableListOf()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","persistentDisposition", 19, EPCISVersion.V2)
    @OpenTraceabilityAttribute("","extension/persistentDisposition", 19, EPCISVersion.V1)
    override var persistentDisposition: PersistentDisposition? = null

    var ILMD: EventILMD? = null


    @OpenTraceabilityXmlIgnoreAttribute
    @OpenTraceabilityAttribute("","type", 0)
    override lateinit var eventType: EventType

    override var products: MutableList<EventProduct> = mutableListOf()

    override fun addProduct(product: EventProduct){
        if (product.Type == EventProductType.Parent)
        {
            if (product.Quantity != null)
            {
                throw  Exception("Parents do not support quantity.");
            }
            this.parentID = product.EPC;
        }
        else if (product.Type == EventProductType.Reference)
        {
            this.referenceProducts.add(product);
        }
        else
        {
            throw Exception("Transaction event only supports references and parents.");
        }
    }
}
