package utility.attributes
import models.identifiers.*
import models.events.*
import java.lang.reflect.Type
class OpenTraceabilityProductsAttribute {
    var Name: String = String()
    var Version: EPCISVersion? = null
    var ProductType: EventProductType = EventProductType()
    var SequenceOrder: Int? = null
    var ListType: OpenTraceabilityProductsListType = OpenTraceabilityProductsListType()
    var Required: Boolean = false
    var TypeId: Object = Object()
    companion object{
    }
}
