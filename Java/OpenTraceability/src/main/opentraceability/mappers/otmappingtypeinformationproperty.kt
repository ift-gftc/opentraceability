package mappers

import models.identifiers.*
import models.events.*
import models.events.EPCISVersion
import models.events.EventProductType
import utility.attributes.*
import java.beans.BeanInfo
import kotlin.reflect.full.createInstance

class OTMappingTypeInformationProperty {

    lateinit var Property: BeanInfo
    var Required: Boolean = false
    var IsObject: Boolean = false
    var IsArray: Boolean = false
    var IsRepeating: Boolean = false
    var IsEPCList: Boolean = false
    var IsQuantityList: Boolean = false
    var ProductType: EventProductType = EventProductType::class.createInstance()
    var Name: String = ""
    var ItemName: String? = null
    var Version: EPCISVersion? = null
    var SequenceOrder: Int? = null
    var CURIEMapping: String? = null



    constructor(
        property: PropertyInfo,
        att: OpenTraceabilityAttribute,
        format: EPCISDataFormat
    ) : this(property, att, format) {
        this.Name = att.name
        this.Version = att.version
        this.SequenceOrder = att.sequenceOrder

        val arrayAttribute = property.getCustomAttribute<OpenTraceabilityArrayAttribute>()
        if (arrayAttribute != null) {
            this.IsArray = true
            this.ItemName = arrayAttribute.itemName
        }

        if (format == EPCISDataFormat.JSON) {
            val jsonAtt = property.getCustomAttribute<OpenTraceabilityJsonAttribute>()
            if (jsonAtt != null) {
                this.Name = jsonAtt.name
            }
        }
    }

    constructor(
        property: PropertyInfo,
        att: OpenTraceabilityJsonAttribute,
        format: EPCISDataFormat
    ) : this(property, att, format) {
        this.Name = att.name

        val arrayAttribute = property.getCustomAttribute<OpenTraceabilityArrayAttribute>()
        if (arrayAttribute != null) {
            this.IsArray = true
            this.ItemName = arrayAttribute.itemName
        }

        if (format == EPCISDataFormat.JSON) {
            val jsonAtt = property.getCustomAttribute<OpenTraceabilityJsonAttribute>()
            if (jsonAtt != null) {
                this.Name = jsonAtt.name
            }
        }
    }

    constructor(
        property: PropertyInfo,
        att: OpenTraceabilityProductsAttribute,
        format: EPCISDataFormat
    ) : this(property, att, format) {
        this.Name = att.name
        this.Version = att.version
        this.SequenceOrder = att.sequenceOrder
        this.IsEPCList = att.listType == OpenTraceabilityProductsListType.EPCList
        this.IsQuantityList = att.listType == OpenTraceabilityProductsListType.QuantityList
        this.ProductType = att.productType
        this.Required = att.required

        if (format == EPCISDataFormat.JSON) {
            val jsonAtt = property.getCustomAttribute<OpenTraceabilityJsonAttribute>()
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
