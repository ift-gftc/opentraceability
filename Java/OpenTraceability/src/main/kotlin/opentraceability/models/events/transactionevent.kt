package models.events

import interfaces.IEvent
import java.util.*
import models.identifiers.EPC
import utility.attributes.*
import java.net.URI

abstract class TransactionEvent : EventBase(), IEvent {

    @OpenTraceabilityAttribute("","parentID", 8)
    var ParentID: EPC? = null

    @OpenTraceabilityProductsAttribute("extension/quantityList", EPCISVersion.V1, EventProductType.Reference, 20, OpenTraceabilityProductsListType.QuantityList)
    @OpenTraceabilityProductsAttribute("quantityList", EPCISVersion.V2, EventProductType.Reference, 15, OpenTraceabilityProductsListType.QuantityList)
    @OpenTraceabilityProductsAttribute("epcList",EPCISVersion.V2, EventProductType.Reference, 9, OpenTraceabilityProductsListType.EPCList)
    var ReferenceProducts: ArrayList<EventProduct> = ArrayList<EventProduct>()

    @OpenTraceabilityAttribute("","action", 10)
    override var Action: EventAction? = null

    @OpenTraceabilityAttribute("","bizStep", 11)
    override var BusinessStep: URI? = null

    @OpenTraceabilityAttribute("","disposition", 12)
    override var Disposition: URI? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","readPoint", 13)
    override var ReadPoint: EventReadPoint = EventReadPoint()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","bizLocation", 14)
    lateinit override var Location: EventLocation

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("bizTransaction")
    @OpenTraceabilityAttribute("","bizTransactionList", 7)
    override var BizTransactionList: ArrayList<EventBusinessTransaction> = ArrayList<EventBusinessTransaction>()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("source")
    @OpenTraceabilityAttribute("","sourceList", 16, EPCISVersion.V2)
    @OpenTraceabilityAttribute("","extension/sourceList", 21, EPCISVersion.V1)
    override var SourceList: ArrayList<EventSource> = ArrayList<EventSource>()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("destination")
    @OpenTraceabilityAttribute("","destinationList", 17, EPCISVersion.V2)
    @OpenTraceabilityAttribute("","extension/destinationList", 22, EPCISVersion.V1)
    override var DestinationList: ArrayList<EventDestination> = ArrayList<EventDestination>()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("sensorElement")
    @OpenTraceabilityAttribute("","sensorElementList", 18, EPCISVersion.V2)
    @OpenTraceabilityAttribute("","extension/sensorElementList",18, EPCISVersion.V1)
    override var SensorElementList: ArrayList<SensorElement> = ArrayList<SensorElement>()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","persistentDisposition", 19, EPCISVersion.V2)
    @OpenTraceabilityAttribute("","extension/persistentDisposition", 19, EPCISVersion.V1)
    override var PersistentDisposition: PersistentDisposition? = null

    var ILMD: EventILMD? = null


    @OpenTraceabilityXmlIgnoreAttribute
    @OpenTraceabilityAttribute("","type", 0)
    override lateinit var EventType: EventType

    override var Products: ArrayList<EventProduct> = ArrayList<EventProduct>()

    override fun AddProduct(product: EventProduct){
        if (product.Type == EventProductType.Parent)
        {
            if (product.Quantity != null)
            {
                throw  Exception("Parents do not support quantity.");
            }
            this.ParentID = product.EPC;
        }
        else if (product.Type == EventProductType.Reference)
        {
            this.ReferenceProducts.add(product);
        }
        else
        {
            throw Exception("Transaction event only supports references and parents.");
        }
    }
}
