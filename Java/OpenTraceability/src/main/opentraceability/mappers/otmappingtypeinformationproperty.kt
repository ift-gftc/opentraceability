package mappers
import models.identifiers.*
import models.events.*
import java.lang.reflect.Type
import java.net.URI
class OTMappingTypeInformationProperty {
    var Property: PropertyInfo = PropertyInfo()
    var Required: Boolean = Boolean()
    var IsObject: Boolean = Boolean()
    var IsArray: Boolean = Boolean()
    var IsRepeating: Boolean = Boolean()
    var IsEPCList: Boolean = Boolean()
    var IsQuantityList: Boolean = Boolean()
    var ProductType: EventProductType = EventProductType()
    var Name: String = String()
    var ItemName: String = String()
    var Version: EPCISVersion? = null
    var SequenceOrder: Int? = null
    var CURIEMapping: String = String()
    companion object{
    }
}
