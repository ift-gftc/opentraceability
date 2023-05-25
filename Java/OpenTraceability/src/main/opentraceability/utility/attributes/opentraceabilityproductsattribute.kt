package utility.attributes

import models.identifiers.*
import models.events.*
import java.lang.reflect.Type

class OpenTraceabilityProductsAttribute /*: Attribute*/ {
    var Name: String = ""
    var Version: EPCISVersion? = null
    lateinit var ProductType: EventProductType
    var SequenceOrder: Int? = null
    lateinit var ListType: OpenTraceabilityProductsListType
    var Required: Boolean = false

    constructor(name: String, version: EPCISVersion, productType: EventProductType, SequenceOrder: Int, listType: OpenTraceabilityProductsListType) {
        this.Name = name
        this.Version = version
        this.ProductType = productType
        this.SequenceOrder = SequenceOrder
        this.ListType = listType
    }

    constructor(name: String,  productType: EventProductType, SequenceOrder: Int, listType: OpenTraceabilityProductsListType) {
        this.Name = name
        this.ProductType = productType
        this.SequenceOrder = SequenceOrder
        this.ListType = listType
    }
}
