package models.events
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

class ObjectEvent<T>: EventBase() {

    //[OpenTraceabilityXmlIgnore]
    //[OpenTraceability("type", 0)]
    lateinit var EventType: EventType

    //[OpenTraceabilityProducts("extension/quantityList", EPCISVersion.V1, EventProductType.Reference, 20, OpenTraceabilityProductsListType.QuantityList)]
    //[OpenTraceabilityProducts("quantityList", EPCISVersion.V2, EventProductType.Reference, 14, OpenTraceabilityProductsListType.QuantityList)]
    //[OpenTraceabilityProducts("epcList", EventProductType.Reference, 7, OpenTraceabilityProductsListType.EPCList, Required = true)]
    var ReferenceProducts: ArrayList<EventProduct> = ArrayList<EventProduct>()

    //[OpenTraceability("action", 8)]
    var Action: EventAction? = null

    //[OpenTraceability("bizStep", 9)]
    var BusinessStep: URI? = null

    //[OpenTraceability("disposition", 10)]
    var Disposition: URI? = null

    //[OpenTraceabilityObject]
    //[OpenTraceability("readPoint", 11)]
    var ReadPoint: EventReadPoint? = null


    //[OpenTraceabilityObject]
    //[OpenTraceability("bizLocation", 12)]
    var Location: EventLocation? = null

    //[OpenTraceabilityObject]
    //[OpenTraceabilityArray("bizTransaction")]
    //[OpenTraceability("bizTransactionList", 13)]
    var BizTransactionList: ArrayList<EventBusinessTransaction> = ArrayList<EventBusinessTransaction>()

    //[OpenTraceabilityObject]
    //[OpenTraceabilityArray("source")]
    //[OpenTraceability("sourceList", 15, EPCISVersion.V2)]
    //[OpenTraceability("extension/sourceList", 21, EPCISVersion.V1)]
    var SourceList: ArrayList<EventSource> = ArrayList<EventSource>()

    //[OpenTraceabilityObject]
    //[OpenTraceabilityArray("destination")]
    //[OpenTraceability("destinationList", 16, EPCISVersion.V2)]
    //[OpenTraceability("extension/destinationList", 22, EPCISVersion.V1)]
    var DestinationList: ArrayList<EventDestination> = ArrayList<EventDestination>()

    //[OpenTraceabilityObject]
    //[OpenTraceabilityArray("sensorElement")]
    //[OpenTraceability("sensorElementList", 17, EPCISVersion.V2)]
    //[OpenTraceability("extension/sensorElementList", EPCISVersion.V1)]
    var SensorElementList: ArrayList<SensorElement> = ArrayList<SensorElement>()

    //[OpenTraceabilityObject]
    //[OpenTraceability("persistentDisposition", 18)]
    //[OpenTraceability("extension/persistentDisposition", EPCISVersion.V1)]
    var PersistentDisposition: PersistentDisposition? = null

    //[OpenTraceabilityObject]
    //[OpenTraceability("ilmd", 19, EPCISVersion.V2)]
    //[OpenTraceability("extension/ilmd", 23, EPCISVersion.V1)]
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
