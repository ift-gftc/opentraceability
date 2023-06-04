package opentraceability.models.events

import opentraceability.interfaces.IEventKDE
import opentraceability.interfaces.ITransformationEvent
import opentraceability.utility.attributes.*
import java.util.*
import java.net.URI

class TransformationEvent<T> : EventBase(), ITransformationEvent {

    @OpenTraceabilityProductsAttribute("inputQuantityList", EPCISVersion.V2, EventProductType.Input, 8, OpenTraceabilityProductsListType.QuantityList)
    @OpenTraceabilityProductsAttribute("inputEPCList", EPCISVersion.V2, EventProductType.Input, 7, OpenTraceabilityProductsListType.EPCList)
    var inputs: MutableList<EventProduct> = mutableListOf()

    var outputs: MutableList<EventProduct> = mutableListOf()

    override var action: EventAction? = null

    @OpenTraceabilityAttribute("", "transformationID", 11)
    override var transformationID: String? = null

    @OpenTraceabilityAttribute("", "bizStep", 12)
    override var businessStep: URI? = null

    @OpenTraceabilityAttribute("", "disposition", 13)
    override var disposition: URI? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "readPoint", 14)
    override var readPoint: EventReadPoint?= null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "bizLocation", 15)
    override var location: EventLocation? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute( "bizTransaction")
    @OpenTraceabilityAttribute("", "bizTransactionList", 16)
    override var bizTransactionList: MutableList<EventBusinessTransaction> = mutableListOf()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute( "source")
    @OpenTraceabilityAttribute("", "sourceList", 17)
    override var sourceList: MutableList<EventSource> = mutableListOf()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute( "destination")
    @OpenTraceabilityAttribute("", "destinationList", 18)
    override var destinationList: MutableList<EventDestination> = mutableListOf()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute( "sensorElement")
    @OpenTraceabilityAttribute("", "sensorElementList", 19)
    override var sensorElementList: MutableList<SensorElement> = mutableListOf()

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "persistentDisposition", 20)
    override var persistentDisposition: PersistentDisposition? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "ilmd", 21)
    var ilmd: T? = null

    @OpenTraceabilityXmlIgnoreAttribute
    @OpenTraceabilityAttribute("", "type", 0)
    override var eventType: EventType = opentraceability.models.events.EventType.TransformationEvent


    override var products: MutableList<EventProduct> = mutableListOf()
        get() {
            val products = mutableListOf<EventProduct>()
            products.addAll(inputs)
            products.addAll(outputs)
            return products
        }

    override fun addProduct(product: EventProduct) {
        when (product.Type) {
            EventProductType.Output -> outputs.add(product)
            EventProductType.Input -> inputs.add(product)
            else -> throw Exception("Transformation event only supports inputs and outputs.")
        }
    }

    override fun <T: IEventKDE> getKDE(clazz: Class<T>, ns: String, name: String): T? {
        return super.getKDE<T>(clazz, ns, name)
    }

    override fun <T: IEventKDE> getKDE(clazz: Class<T>): T? {
        return super.getKDE<T>(clazz)
    }


    override fun grabILMD(): EventILMD? {
        return ilmd as EventILMD
    }
}
