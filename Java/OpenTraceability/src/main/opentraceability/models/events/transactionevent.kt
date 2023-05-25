package models.events

import interfaces.IEvent
import interfaces.IEventKDE
import java.util.*
import models.identifiers.*
import models.events.kdes.CertificationList
import models.events.*
import java.time.Duration
import java.lang.reflect.Type
import java.net.URI
import java.time.OffsetDateTime

//TODO: review this

class TransactionEvent /*: EventBase(), IEvent*/ {

    //[OpenTraceability("parentID", 8)]
    var ParentID: EPC? = null

    //[OpenTraceabilityProducts("extension/quantityList", EPCISVersion.V1, EventProductType.Reference, 20, OpenTraceabilityProductsListType.QuantityList)]
    //[OpenTraceabilityProducts("quantityList", EPCISVersion.V2, EventProductType.Reference, 15, OpenTraceabilityProductsListType.QuantityList)]
    //[OpenTraceabilityProducts("epcList", EventProductType.Reference, 9, OpenTraceabilityProductsListType.EPCList)]
    var ReferenceProducts: ArrayList<EventProduct> = ArrayList<EventProduct>()

    //[OpenTraceability("action", 10)]
    var Action: EventAction? = null

    //[OpenTraceability("bizStep", 11)]
    var BusinessStep: URI? = null

    //[OpenTraceability("disposition", 12)]
    var Disposition: URI? = null

    //[OpenTraceabilityObject]
    //[OpenTraceability("readPoint", 13)]
    var ReadPoint: EventReadPoint = EventReadPoint()

    //[OpenTraceabilityObject]
    //[OpenTraceability("bizLocation", 14)]
    lateinit var Location: EventLocation

    //[OpenTraceabilityObject]
    //[OpenTraceabilityArray("bizTransaction")]
    //[OpenTraceability("bizTransactionList", 7)]
    var BizTransactionList: ArrayList<EventBusinessTransaction> = ArrayList<EventBusinessTransaction>()

    //[OpenTraceabilityObject]
    //[OpenTraceabilityArray("source")]
    //[OpenTraceability("sourceList", 16, EPCISVersion.V2)]
    //[OpenTraceability("extension/sourceList", 21, EPCISVersion.V1)]
    var SourceList: ArrayList<EventSource> = ArrayList<EventSource>()

    //[OpenTraceabilityObject]
    //[OpenTraceabilityArray("destination")]
    //[OpenTraceability("destinationList", 17, EPCISVersion.V2)]
    //[OpenTraceability("extension/destinationList", 22, EPCISVersion.V1)]
    var DestinationList: ArrayList<EventDestination> = ArrayList<EventDestination>()

    //[OpenTraceabilityObject]
    //[OpenTraceabilityArray("sensorElement")]
    //[OpenTraceability("sensorElementList", 18, EPCISVersion.V2)]
    //[OpenTraceability("extension/sensorElementList", EPCISVersion.V1)]
    var SensorElementList: ArrayList<SensorElement> = ArrayList<SensorElement>()

    //[OpenTraceabilityObject]
    //[OpenTraceability("persistentDisposition", 19, EPCISVersion.V2)]
    //[OpenTraceability("extension/persistentDisposition", EPCISVersion.V1)]
    var PersistentDisposition: PersistentDisposition = PersistentDisposition()

    var ILMD: EventILMD? = null


    //[OpenTraceabilityXmlIgnore]
    //[OpenTraceability("type", 0)]
    lateinit var EventType: EventType

    var Products: ArrayList<EventProduct> = ArrayList<EventProduct>()

    fun AddProduct(product: EventProduct){
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
