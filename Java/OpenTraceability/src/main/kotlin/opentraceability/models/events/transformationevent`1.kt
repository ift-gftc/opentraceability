package models.events

import interfaces.ITransformationEvent
import java.util.*
import utility.attributes.*
import java.net.URI


class TransformationEvent<T> : EventBase(), ITransformationEvent {

    @OpenTraceabilityProductsAttribute("inputQuantityList", EPCISVersion.V2, EventProductType.Input, 8, OpenTraceabilityProductsListType.QuantityList)
    @OpenTraceabilityProductsAttribute("inputEPCList", EPCISVersion.V2, EventProductType.Input, 7, OpenTraceabilityProductsListType.EPCList)
    var Inputs: ArrayList<EventProduct> = ArrayList<EventProduct>()

    var Outputs: ArrayList<EventProduct> = ArrayList<EventProduct>()

    override var Action: EventAction? = null

    override var TransformationID: String? = null

    override var BusinessStep: URI? = null

    override var Disposition: URI? = null

    override lateinit var ReadPoint: EventReadPoint

    override lateinit var Location: EventLocation


    override var BizTransactionList: ArrayList<EventBusinessTransaction> = ArrayList<EventBusinessTransaction>()

    override var SourceList: ArrayList<EventSource> = ArrayList<EventSource>()

    override var DestinationList: ArrayList<EventDestination> = ArrayList<EventDestination>()

    override var SensorElementList: ArrayList<SensorElement> = ArrayList<SensorElement>()

    override var PersistentDisposition: PersistentDisposition? = null

    var ILMD: T? = null

    override lateinit var EventType: EventType


    override var Products: ArrayList<EventProduct> = ArrayList<EventProduct>()

    override fun AddProduct(product: EventProduct) {
        when (product.Type) {
            EventProductType.Output -> Outputs.add(product)
            EventProductType.Input -> Inputs.add(product)
            else -> throw Exception("Transformation event only supports inputs and outputs.")
        }
    }

}
