package mappers

import java.util.*
import java.lang.reflect.Type

class OTMappingTypeInformation {
    companion object {
    }

    var Type: Type = Type()
    var Properties: List<OTMappingTypeInformationProperty> = ArrayList<OTMappingTypeInformationProperty>()
    var ExtensionKDEs: PropertyInfo = PropertyInfo()
    var ExtensionAttributes: PropertyInfo = PropertyInfo()
    var Item: OTMappingTypeInformationProperty = OTMappingTypeInformationProperty()


    fun GetXmlTypeInfo(t: Type): OTMappingTypeInformation {
        // Method body goes here
        return OTMappingTypeInformation()
    }

    fun GetJsonTypeInfo(t: Type): OTMappingTypeInformation {
        // Method body goes here
        return OTMappingTypeInformation()
    }

    fun GetMasterDataXmlTypeInfo(t: Type): OTMappingTypeInformation {
        // Method body goes here
        return OTMappingTypeInformation()
    }

    fun GetMasterDataJsonTypeInfo(t: Type): OTMappingTypeInformation {
        // Method body goes here
        return OTMappingTypeInformation()
    }
}
