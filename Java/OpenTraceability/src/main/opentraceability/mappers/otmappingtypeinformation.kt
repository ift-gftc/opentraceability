package opentraceability.mappers

import java.beans.BeanInfo
import java.util.*
import java.lang.reflect.Type
import kotlin.reflect.KProperty

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

    lateinit var Type: Type
    var Properties: ArrayList<OTMappingTypeInformationProperty> = ArrayList<OTMappingTypeInformationProperty>()
    var ExtensionKDEs: BeanInfo? = null
    var ExtensionAttributes: BeanInfo? = null

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
