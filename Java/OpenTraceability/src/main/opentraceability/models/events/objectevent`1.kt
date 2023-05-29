package opentraceability.models.events
import opentraceability.interfaces.IEventKDE
import java.util.*
import opentraceability.models.identifiers.*
import opentraceability.models.events.kdes.CertificationList
import opentraceability.models.events.*
import opentraceability.utility.attributes.OpenTraceabilityAttribute
import opentraceability.utility.attributes.OpenTraceabilityObjectAttribute
import opentraceability.utility.attributes.OpenTraceabilityXmlIgnoreAttribute
import java.time.Duration
import java.lang.reflect.Type
import java.net.URI
import java.time.OffsetDateTime

//TODO: review this

class ObjectEvent<T>: EventBase() {

    @OpenTraceabilityXmlIgnoreAttribute
    @OpenTraceabilityAttribute("","type", 0)
    lateinit var EventType: EventType

    //[OpenTraceabilityProducts("extension/quantityList", EPCISVersion.V1, EventProductType.Reference, 20, OpenTraceabilityProductsListType.QuantityList)
    //[OpenTraceabilityProducts("quantityList", EPCISVersion.V2, EventProductType.Reference, 14, OpenTraceabilityProductsListType.QuantityList)
    //[OpenTraceabilityProducts("epcList", EventProductType.Reference, 7, OpenTraceabilityProductsListType.EPCList, Required = true)
    var ReferenceProducts: ArrayList<EventProduct> = ArrayList<EventProduct>()

    @OpenTraceabilityAttribute("","action", 8)
    var Action: EventAction? = null

    @OpenTraceabilityAttribute("","bizStep", 9)
    var BusinessStep: URI? = null

    @OpenTraceabilityAttribute("","disposition", 10)
    var Disposition: URI? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","readPoint", 11)
    var ReadPoint: EventReadPoint? = null


    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","bizLocation", 12)
    var Location: EventLocation? = null

    @OpenTraceabilityObjectAttribute
    //[OpenTraceabilityArray("bizTransaction")
    @OpenTraceabilityAttribute("","bizTransactionList", 13)
    var BizTransactionList: ArrayList<EventBusinessTransaction> = ArrayList<EventBusinessTransaction>()

    @OpenTraceabilityObjectAttribute
    //[OpenTraceabilityArray("source")
    @OpenTraceabilityAttribute("","sourceList", 15, EPCISVersion.V2)
    //@OpenTraceabilityAttribute("","extension/sourceList", 21, EPCISVersion.V1)
    var SourceList: ArrayList<EventSource> = ArrayList<EventSource>()

    @OpenTraceabilityObjectAttribute
    //[OpenTraceabilityArray("destination")
    @OpenTraceabilityAttribute("","destinationList", 16, EPCISVersion.V2)
    //@OpenTraceabilityAttribute("","extension/destinationList", 22, EPCISVersion.V1)
    var DestinationList: ArrayList<EventDestination> = ArrayList<EventDestination>()

    @OpenTraceabilityObjectAttribute
    //[OpenTraceabilityArray("sensorElement")
    @OpenTraceabilityAttribute("","sensorElementList", 17, EPCISVersion.V2)
    //@OpenTraceabilityAttribute("","extension/sensorElementList",  EPCISVersion.V1)
    var SensorElementList: ArrayList<SensorElement> = ArrayList<SensorElement>()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","persistentDisposition", 18)
    //@OpenTraceabilityAttribute("","extension/persistentDisposition", EPCISVersion.V1)
    var PersistentDisposition: PersistentDisposition? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("","ilmd", 19, EPCISVersion.V2)
    //@OpenTraceabilityAttribute("","extension/ilmd", 23, EPCISVersion.V1)
    var ILMD: T? = null

    var Products: ArrayList<EventProduct> = ArrayList<EventProduct>()

    /*
    public ReadOnlyCollection<EventProduct> Products
    {
        get
        {
            ArrayList<EventProduct> products = new ArrayList<EventProduct>();
            products.AddRange(this.ReferenceProducts);
            return new ReadOnlyCollection<EventProduct>(products);
        }
    }

    public void AddProduct(EventProduct product)
    {
        if (product.Type == EventProductType.Reference)
        {
            this.ReferenceProducts.Add(product);
        }
        else
        {
            throw new Exception("Object event only supports references.");
        }
    }
    */
}
