package mappers

import models.identifiers.*
import models.events.*
import kotlin.reflect.full.createInstance

class OTMappingTypeInformationProperty {

    var Property: PropertyInfo = PropertyInfo()
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
