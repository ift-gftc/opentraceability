package models.events

import interfaces.IEventKDE
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

    @OpenTraceabilityAttribute("", "transformationID", 11)
    override var TransformationID: String? = null

    @OpenTraceabilityAttribute("", "bizStep", 12)
    override var BusinessStep: URI? = null

    @OpenTraceabilityAttribute("", "disposition", 13)
    override var Disposition: URI? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "readPoint", 14)
    override var ReadPoint: EventReadPoint?= null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "bizLocation", 15)
    override var Location: EventLocation? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute( "bizTransaction")
    @OpenTraceabilityAttribute("", "bizTransactionList", 16)
    override var BizTransactionList: MutableList<EventBusinessTransaction> = mutableListOf()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute( "source")
    @OpenTraceabilityAttribute("", "sourceList", 17)
    override var SourceList: MutableList<EventSource> = mutableListOf()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute( "destination")
    @OpenTraceabilityAttribute("", "destinationList", 18)
    override var DestinationList: MutableList<EventDestination> = mutableListOf()


    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute( "sensorElement")
    @OpenTraceabilityAttribute("", "sensorElementList", 19)
    override var SensorElementList: MutableList<SensorElement> = mutableListOf()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "persistentDisposition", 20)
    override var PersistentDisposition: PersistentDisposition? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "ilmd", 21)
    var ILMD: T? = null

    @OpenTraceabilityXmlIgnoreAttribute
    @OpenTraceabilityAttribute("", "type", 0)
    override var EventType: EventType = models.events.EventType.TransformationEvent


    override var Products: MutableList<EventProduct> = mutableListOf()
        get() {
            val products = mutableListOf<EventProduct>()
            products.addAll(Inputs)
            products.addAll(Outputs)
            return products
        }

    override fun AddProduct(product: EventProduct) {
        when (product.Type) {
            EventProductType.Output -> Outputs.add(product)
            EventProductType.Input -> Inputs.add(product)
            else -> throw Exception("Transformation event only supports inputs and outputs.")
        }
    }

    override fun <T: IEventKDE> GetKDE(clazz: Class<T>, ns: String, name: String): T? {
        return super.GetKDE<T>(clazz, ns, name)
    }

    override fun <T: IEventKDE> GetKDE(clazz: Class<T>): T? {
        return super.GetKDE<T>(clazz)
    }


    override fun GetILMD(): EventILMD? {
        return ILMD as EventILMD
    }
}
