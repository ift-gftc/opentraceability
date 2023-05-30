package models.events

import interfaces.IEventKDE
import interfaces.ITransformationEvent
import java.util.*
import models.identifiers.*
import models.events.kdes.CertificationList
import models.events.*
import utility.attributes.OpenTraceabilityProductsAttribute
import utility.attributes.OpenTraceabilityProductsListType
import java.time.Duration
import java.lang.reflect.Type
import java.net.URI
import java.time.OffsetDateTime


class TransformationEvent<T> : EventBase, ITransformationEvent {

    @OpenTraceabilityProductsAttribute("inputQuantityList", EventProductType.Input, 8, OpenTraceabilityProductsListType.QuantityList)
    @OpenTraceabilityProductsAttribute("inputEPCList", EventProductType.Input, 7, OpenTraceabilityProductsListType.EPCList)
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


    val Products: List<EventProduct>
        get() {
            val products = mutableListOf<EventProduct>()
            products.addAll(Inputs)
            products.addAll(Outputs)
            return ReadOnlyCollection(products)
        }

    fun AddProduct(product: EventProduct) {
        when (product.type) {
            EventProductType.Output -> Outputs.add(product)
            EventProductType.Input -> Inputs.add(product)
            else -> throw Exception("Transformation event only supports inputs and outputs.")
        }
    }

}
