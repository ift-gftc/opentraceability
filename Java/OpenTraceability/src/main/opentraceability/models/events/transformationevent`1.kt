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

class TransformationEvent<T>: EventBase, ITransformationEvent, {

    //[OpenTraceabilityProducts("inputQuantityList", EventProductType.Input, 8, OpenTraceabilityProductsListType.QuantityList)]
    //[OpenTraceabilityProducts("inputEPCList", EventProductType.Input, 7, OpenTraceabilityProductsListType.EPCList)]
    var Inputs: List<EventProduct> = ArrayList<EventProduct>()

    var Outputs: List<EventProduct> = ArrayList<EventProduct>()

    var Action: EventAction? = null

    var TransformationID: String? = null

    var BusinessStep: URI? = null

    var Disposition: URI? = null

    var ReadPoint: EventReadPoint? = null

    var Location: EventLocation? = null


    var BizTransactionList: List<EventBusinessTransaction> = ArrayList<EventBusinessTransaction>()

    var SourceList: List<EventSource> = ArrayList<EventSource>()

    var DestinationList: List<EventDestination> = ArrayList<EventDestination>()

    var SensorElementList: List<SensorElement> = ArrayList<SensorElement>()

    var PersistentDisposition: PersistentDisposition? = null

    var ILMD: T? = null

    var EventType: EventType = EventType()

    //public EventILMD? GetILMD() => ILMD;



    var Products: List<EventProduct> = ArrayList<EventProduct>()
    /*
    public ReadOnlyCollection<EventProduct> Products
    {
        get
        {
            List<EventProduct> products = new List<EventProduct>();
            products.AddRange(Inputs);
            products.AddRange(Outputs);
            return new ReadOnlyCollection<EventProduct>(products);
        }
    }
    */

    /*
    public void AddProduct(EventProduct product)
    {
        if (product.Type == EventProductType.Output)
        {
            this.Outputs.Add(product);
        }
        else if (product.Type == EventProductType.Input)
        {
            this.Inputs.Add(product);
        }
        else
        {
            throw new Exception("Transformation event only supports inputs and outputs.");
        }
    }
    */
}
