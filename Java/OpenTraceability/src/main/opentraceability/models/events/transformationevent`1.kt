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

class TransformationEvent<T> /*: EventBase, ITransformationEvent*/ {

    //[OpenTraceabilityProducts("inputQuantityList", EventProductType.Input, 8, OpenTraceabilityProductsListType.QuantityList)
    //[OpenTraceabilityProducts("inputEPCList", EventProductType.Input, 7, OpenTraceabilityProductsListType.EPCList)
    var Inputs: ArrayList<EventProduct> = ArrayList<EventProduct>()

    var Outputs: ArrayList<EventProduct> = ArrayList<EventProduct>()

    var Action: EventAction? = null

    var TransformationID: String? = null

    var BusinessStep: URI? = null

    var Disposition: URI? = null

    var ReadPoint: EventReadPoint? = null

    var Location: EventLocation? = null


    var BizTransactionList: ArrayList<EventBusinessTransaction> = ArrayList<EventBusinessTransaction>()

    var SourceList: ArrayList<EventSource> = ArrayList<EventSource>()

    var DestinationList: ArrayList<EventDestination> = ArrayList<EventDestination>()

    var SensorElementList: ArrayList<SensorElement> = ArrayList<SensorElement>()

    var PersistentDisposition: PersistentDisposition? = null

    var ILMD: T? = null

    lateinit var EventType: EventType

    //public EventILMD? GetILMD() => ILMD;



    var Products: ArrayList<EventProduct> = ArrayList<EventProduct>()
    /*
    public ReadOnlyCollection<EventProduct> Products
    {
        get
        {
            ArrayList<EventProduct> products = new ArrayList<EventProduct>();
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
