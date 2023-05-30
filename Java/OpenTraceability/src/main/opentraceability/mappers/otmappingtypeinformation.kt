package mappers

import utility.attributes.*
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

        fun GetXmlTypeInfo(t: KClass<*>): OTMappingTypeInformation {
            if (!_XmlTypeInfos.containsKey(t)) {
                synchronized(_locker) {
                    if (!_XmlTypeInfos.containsKey(t)) {
                        val typeInfo = OTMappingTypeInformation(t, EPCISDataFormat.XML)
                        _XmlTypeInfos[t] = typeInfo
                    }
                }
            }
            return _XmlTypeInfos[t]!!
        }

        fun GetJsonTypeInfo(t: KClass<*>): OTMappingTypeInformation {
            if (!_JsonTypeInfos.containsKey(t)) {
                synchronized(_locker) {
                    if (!_JsonTypeInfos.containsKey(t)) {
                        val typeInfo = OTMappingTypeInformation(t, EPCISDataFormat.JSON)
                        _JsonTypeInfos[t] = typeInfo
                    }
                }
            }
            return _JsonTypeInfos[t]!!
        }

        fun GetMasterDataXmlTypeInfo(t: KClass<*>): OTMappingTypeInformation {
            if (!_masterDataXmlTypeInfos.containsKey(t)) {
                synchronized(_locker) {
                    if (!_masterDataXmlTypeInfos.containsKey(t)) {
                        val typeInfo = OTMappingTypeInformation(t, EPCISDataFormat.XML, true)
                        _masterDataXmlTypeInfos[t] = typeInfo
                    }
                }
            }
            return _masterDataXmlTypeInfos[t]!!
        }

        fun GetMasterDataJsonTypeInfo(t: KClass<*>): OTMappingTypeInformation {
            if (!_masterDataJsonTypeInfos.containsKey(t)) {
                synchronized(_locker) {
                    if (!_masterDataJsonTypeInfos.containsKey(t)) {
                        val typeInfo = OTMappingTypeInformation(t, EPCISDataFormat.JSON, true)
                        _masterDataJsonTypeInfos[t] = typeInfo
                    }
                }
            }
            return _masterDataJsonTypeInfos[t]!!
        }

    }

    lateinit var Type: Type
    var Properties: ArrayList<OTMappingTypeInformationProperty> = ArrayList<OTMappingTypeInformationProperty>()
    var ExtensionKDEs: BeanInfo? = null
    var ExtensionAttributes: BeanInfo? = null

    class OTMappingTypeInformation(val type: KClass<*>, format: EPCISDataFormat, isMasterDataMapping: Boolean = false) {
        val properties: MutableList<OTMappingTypeInformationProperty> = mutableListOf()
        private val _dic: MutableMap<String, OTMappingTypeInformationProperty> = mutableMapOf()
        var extensionKDEs: KProperty<*>? = null
        var extensionAttributes: KProperty<*>? = null

        init {
            type.memberProperties.forEach { p ->
                if (format == EPCISDataFormat.XML && p.findAnnotation<OpenTraceabilityXmlIgnoreAttribute>() != null) {
                    return@forEach
                }

                if (isMasterDataMapping) {
                    val mdAtt = p.findAnnotation<OpenTraceabilityMasterDataAttribute>()

                    if (mdAtt != null) {
                        val property = OTMappingTypeInformationProperty(p, mdAtt, format)
                        properties.add(property)
                        _dic[property.Name] = property
                    }
                } else {
                    val atts = p.annotations.filterIsInstance<OpenTraceabilityAttribute>()
                    val jsonAtt = p.findAnnotation<OpenTraceabilityJsonAttribute>()
                    val productAtts = p.annotations.filterIsInstance<OpenTraceabilityProductsAttribute>()

                    if (atts.isNotEmpty()) {
                        atts.forEach { att ->
                            val property = OTMappingTypeInformationProperty(p, att, format)
                            if (!_dic.containsKey(property.Name)) {
                                properties.add(property)
                                _dic[property.Name] = property
                            }
                        }
                    } else if (jsonAtt != null && format == EPCISDataFormat.JSON) {
                        val property = OTMappingTypeInformationProperty(p, jsonAtt, format)
                        if (!_dic.containsKey(property.Name)) {
                            properties.add(property)
                            _dic[property.Name] = property
                        }
                    } else if (productAtts.isNotEmpty()) {
                        productAtts.forEach { att ->
                            val property = OTMappingTypeInformationProperty(p, att, format)
                            if (!_dic.containsKey(property.Name)) {
                                properties.add(property)
                                _dic[property.Name] = property
                            }
                        }
                    } else if (p.findAnnotation<OpenTraceabilityExtensionElementsAttribute>() != null) {
                        extensionKDEs = p
                    } else if (p.findAnnotation<OpenTraceabilityExtensionAttributesAttribute>() != null) {
                        extensionAttributes = p
                    }

                    properties.sortBy { p -> p.SequenceOrder == null }
                    properties.sortBy { p -> p.SequenceOrder }
                }
            }
        }

        operator fun get(name: String): OTMappingTypeInformationProperty? {
            return _dic[name]
        }
    }


}
