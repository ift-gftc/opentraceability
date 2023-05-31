package mappers

import models.events.*
import utility.attributes.*
import java.beans.BeanInfo
import kotlin.reflect.KProperty

class OTMappingTypeInformationProperty {

    lateinit var Property: BeanInfo
    var Required: Boolean = false
    var IsObject: Boolean = false
    var IsArray: Boolean = false
    var IsRepeating: Boolean = false
    var IsEPCList: Boolean = false
    var IsQuantityList: Boolean = false
    var ProductType: EventProductType? = null
    var Name: String = ""
    var ItemName: String? = null
    var Version: EPCISVersion? = null
    var SequenceOrder: Int? = null
    var CURIEMapping: String? = null



    constructor(property: KProperty<*>, att: OpenTraceabilityAttribute, format: EPCISDataFormat ) {
        this.Name = att.name
        this.Version = att.version
        this.SequenceOrder = att.sequenceOrder

        val arrayAttribute = property.annotations
            .filterIsInstance<OpenTraceabilityArrayAttribute>()
            .firstOrNull()

        if (arrayAttribute != null) {
            this.IsArray = true
            this.ItemName = arrayAttribute.itemName
        }

        if (format == EPCISDataFormat.JSON) {
            val jsonAtt = property.annotations
                .filterIsInstance<OpenTraceabilityJsonAttribute>()
                .firstOrNull()

            if (jsonAtt != null) {
                this.Name = jsonAtt.name
            }
        }
    }

    constructor(
        property: KProperty<*>,
        att: OpenTraceabilityJsonAttribute,
        format: EPCISDataFormat
    ) {
        this.Name = att.name

        val arrayAttribute = property.annotations
            .filterIsInstance<OpenTraceabilityArrayAttribute>()
            .firstOrNull()


        if (arrayAttribute != null) {
            this.IsArray = true
            this.ItemName = arrayAttribute.itemName
        }

        if (format == EPCISDataFormat.JSON) {
            val jsonAtt = property.annotations
                .filterIsInstance<OpenTraceabilityJsonAttribute>()
                .firstOrNull()

            if (jsonAtt != null) {
                this.Name = jsonAtt.name
            }
        }
    }

    constructor(
        property: KProperty<*>,
        att: OpenTraceabilityProductsAttribute,
        format: EPCISDataFormat
    )  {
        this.Name = att.name
        this.Version = att.version
        this.SequenceOrder = att.sequenceOrder
        this.IsEPCList = att.listType == OpenTraceabilityProductsListType.EPCList
        this.IsQuantityList = att.listType == OpenTraceabilityProductsListType.QuantityList
        this.ProductType = att.productType
        this.Required = att.required

        if (format == EPCISDataFormat.JSON) {
            val jsonAtt = property.annotations
                .filterIsInstance<OpenTraceabilityJsonAttribute>()
                .firstOrNull()

            if (jsonAtt != null) {
                this.Name = jsonAtt.name
            }
        }
    }

    var required: Boolean = false
    var isEPCList: Boolean = false
    var isQuantityList: Boolean = false
    var productType: EventProductType? = null
    var version: EPCISVersion? = null
    var sequenceOrder: Int? = null
    var curieMapping: String? = null
}
