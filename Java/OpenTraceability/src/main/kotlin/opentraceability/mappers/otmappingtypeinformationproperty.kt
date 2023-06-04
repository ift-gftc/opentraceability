package opentraceability.mappers

import opentraceability.models.events.*
import opentraceability.utility.attributes.*
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

class OTMappingTypeInformationProperty {

    lateinit var Property: KMutableProperty<*>
    var Required: Boolean = false
    var IsObject: Boolean = false
    var IsArray: Boolean = false
    var IsRepeating: Boolean = false
    var IsEPCList: Boolean = false
    var IsQuantityList: Boolean = false
    var ProductType: EventProductType = EventProductType.Reference
    var Name: String = ""
    var ItemName: String? = null
    var Version: EPCISVersion? = null
    var SequenceOrder: Int? = null
    var CURIEMapping: String? = null

    constructor(property: KMutableProperty<*>, att: OpenTraceabilityMasterDataAttribute, format: EPCISDataFormat ) {
        this.Name = att.name
        this.Property = property
        this.IsObject = property.annotations.filterIsInstance<OpenTraceabilityObjectAttribute>().isNotEmpty()
        this.IsRepeating = property.annotations.filterIsInstance<OpenTraceabilityRepeatingAttribute>().isNotEmpty()

        val arrayAttribute = property.annotations
            .filterIsInstance<OpenTraceabilityArrayAttribute>()
            .firstOrNull()

        if (arrayAttribute != null) {
            this.IsArray = true
            this.ItemName = arrayAttribute.itemName
        }
    }

    constructor(property: KMutableProperty<*>, att: OpenTraceabilityAttribute, format: EPCISDataFormat ) {
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
        property: KMutableProperty<*>,
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
        property: KMutableProperty<*>,
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
}
