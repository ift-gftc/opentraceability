package opentraceability.mappers

import opentraceability.models.identifiers.*
import opentraceability.models.events.*
import opentraceability.models.events.EPCISVersion
import opentraceability.models.events.EventProductType
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

}
