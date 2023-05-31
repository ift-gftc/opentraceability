package utility.attributes

import models.events.EPCISVersion
import models.events.EventProductType
import java.lang.reflect.Type

@Repeatable
annotation class OpenTraceabilityProductsAttribute(
    val name: String,
    val version: EPCISVersion = EPCISVersion.V2,
    val productType: EventProductType,
    val sequenceOrder: Int = -1,
    val listType: OpenTraceabilityProductsListType,
    val required: Boolean = false
) {
}
