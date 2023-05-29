package opentraceability.models.events

import opentraceability.interfaces.IEvent
import opentraceability.interfaces.IEventKDE
import java.util.*
import opentraceability.models.identifiers.*
import opentraceability.models.events.kdes.CertificationList
import opentraceability.models.events.*
import opentraceability.models.identifiers.EPC
import opentraceability.utility.attributes.OpenTraceabilityAttribute
import opentraceability.utility.attributes.OpenTraceabilityObjectAttribute
import opentraceability.utility.attributes.OpenTraceabilityXmlIgnoreAttribute
import java.time.Duration
import java.lang.reflect.Type
import java.net.URI
import java.time.OffsetDateTime

//TODO: review this

class TransactionEvent /*: EventBase(), IEvent*/ {

    @OpenTraceabilityAttribute("","parentID", 8)
    var ParentID: EPC? = null

    //[OpenTraceabilityProducts("extension/quantityList", EPCISVersion.V1, EventProductType.Reference, 20, OpenTraceabilityProductsListType.QuantityList)
    //[OpenTraceabilityProducts("quantityList", EPCISVersion.V2, EventProductType.Reference, 15, OpenTraceabilityProductsListType.QuantityList)
    //[OpenTraceabilityProducts("epcList", EventProductType.Reference, 9, OpenTraceabilityProductsListType.EPCList)
    var ReferenceProducts: ArrayList<EventProduct> = ArrayList<EventProduct>()

    @OpenTraceabilityAttribute("","action", 10)
    var Action: EventAction? = null

    @OpenTraceabilityAttribute("","bizStep", 11)
    var BusinessStep: URI? = null

    @OpenTraceabilityAttribute("","disposition", 12)
    var Disposition: URI? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","readPoint", 13)
    var ReadPoint: EventReadPoint = EventReadPoint()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","bizLocation", 14)
    lateinit var Location: EventLocation

    @OpenTraceabilityObjectAttribute
    //[OpenTraceabilityArray("bizTransaction")
    @OpenTraceabilityAttribute("","bizTransactionList", 7)
    var BizTransactionList: ArrayList<EventBusinessTransaction> = ArrayList<EventBusinessTransaction>()

    @OpenTraceabilityObjectAttribute
    //[OpenTraceabilityArray("source")
    @OpenTraceabilityAttribute("","sourceList", 16, EPCISVersion.V2)
    //@OpenTraceabilityAttribute("","extension/sourceList", 21, EPCISVersion.V1)
    var SourceList: ArrayList<EventSource> = ArrayList<EventSource>()

    @OpenTraceabilityObjectAttribute
    //[OpenTraceabilityArray("destination")
    @OpenTraceabilityAttribute("","destinationList", 17, EPCISVersion.V2)
    //@OpenTraceabilityAttribute("","extension/destinationList", 22, EPCISVersion.V1)
    var DestinationList: ArrayList<EventDestination> = ArrayList<EventDestination>()

    @OpenTraceabilityObjectAttribute
    //[OpenTraceabilityArray("sensorElement")
    @OpenTraceabilityAttribute("","sensorElementList", 18, EPCISVersion.V2)
    //@OpenTraceabilityAttribute("","extension/sensorElementList", EPCISVersion.V1)
    var SensorElementList: ArrayList<SensorElement> = ArrayList<SensorElement>()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","persistentDisposition", 19, EPCISVersion.V2)
    //@OpenTraceabilityAttribute("","extension/persistentDisposition", EPCISVersion.V1)
    var PersistentDisposition: PersistentDisposition = PersistentDisposition()

    var ILMD: EventILMD? = null


    @OpenTraceabilityXmlIgnoreAttribute
    @OpenTraceabilityAttribute("","type", 0)
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
