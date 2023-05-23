package mappers

import java.util.*
import java.lang.reflect.Type

class OTMappingTypeInformation {
    companion object {
        var _XmlTypeInfos: MutableMap<Type, OTMappingTypeInformation> = mutableMapOf()
        var _JsonTypeInfos: MutableMap<Type, OTMappingTypeInformation> = mutableMapOf()
        var _masterDataXmlTypeInfos: MutableMap<Type, OTMappingTypeInformation> = mutableMapOf()
        var _masterDataJsonTypeInfos: MutableMap<Type, OTMappingTypeInformation> = mutableMapOf()

        fun GetXmlTypeInfo(t: Type): OTMappingTypeInformation {
            TODO("Not yet implemented")
        }

        fun GetJsonTypeInfo(t: Type): OTMappingTypeInformation {
            TODO("Not yet implemented")
        }

        fun GetMasterDataXmlTypeInfo(t: Type): OTMappingTypeInformation {
            TODO("Not yet implemented")
        }

        fun GetMasterDataJsonTypeInfo(t: Type): OTMappingTypeInformation {
            TODO("Not yet implemented")
        }
    }

    var Type: Type = Type()
    var Properties: List<OTMappingTypeInformationProperty> = ArrayList<OTMappingTypeInformationProperty>()
    var ExtensionKDEs: PropertyInfo? = PropertyInfo()
    var ExtensionAttributes: PropertyInfo? = PropertyInfo()

    //TODO: missing property
    /*
    public OTMappingTypeInformationProperty? this[string name]
    {
        get
        {
            if (_dic.ContainsKey(name))
            {
                return _dic[name];
            }
            else
            {
                return null;
            }
        }
    }
    */

}
