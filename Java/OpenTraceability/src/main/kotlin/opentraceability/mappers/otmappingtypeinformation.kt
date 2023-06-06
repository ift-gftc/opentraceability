package opentraceability.mappers

import opentraceability.utility.attributes.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.reflect.*
import kotlin.reflect.full.memberProperties

class OTMappingTypeInformation {
    lateinit var Type: KClass<*>
    val properties: MutableList<OTMappingTypeInformationProperty> = mutableListOf()
    val dic: MutableMap<String, OTMappingTypeInformationProperty> = mutableMapOf()
    var extensionKDEs: KMutableProperty<*>? = null
    var extensionAttributes: KMutableProperty<*>? = null

    constructor() { }

    constructor(type: KClass<*>, format: EPCISDataFormat, isMasterDataMapping: Boolean = false) {
        Type = type

        type.memberProperties.forEach { p ->
            val kprop = p as? KMutableProperty<*>
            if (kprop != null) {
                if (format == EPCISDataFormat.XML && kprop.annotations.filterIsInstance<OpenTraceabilityXmlIgnoreAttribute>()
                        .isNotEmpty()
                ) {
                    return@forEach
                }

                if (isMasterDataMapping) {
                    val mdAtt = kprop.annotations.filterIsInstance<OpenTraceabilityMasterDataAttribute>()
                    if (mdAtt != null && mdAtt.count() > 0) {
                        val property = OTMappingTypeInformationProperty(kprop, mdAtt.first(), format)
                        properties.add(property)
                        dic[property.Name] = property
                    }
                } else {
                    val atts = kprop.annotations.filterIsInstance<OpenTraceabilityAttribute>()
                    val jsonAtt = kprop.annotations.filterIsInstance<OpenTraceabilityJsonAttribute>()
                    val productAtts = kprop.annotations.filterIsInstance<OpenTraceabilityProductsAttribute>()

                    if (atts.isNotEmpty()) {
                        atts.forEach { att ->
                            val property = OTMappingTypeInformationProperty(kprop, att, format)
                            if (!dic.containsKey(property.Name)) {
                                properties.add(property)
                                dic[property.Name] = property
                            }
                        }
                    } else if (jsonAtt != null && jsonAtt.isNotEmpty() && format == EPCISDataFormat.JSON) {
                        val property = OTMappingTypeInformationProperty(kprop, jsonAtt.first(), format)
                        if (!dic.containsKey(property.Name)) {
                            properties.add(property)
                            dic[property.Name] = property
                        }
                    } else if (productAtts.isNotEmpty()) {
                        productAtts.forEach { att ->
                            val property = OTMappingTypeInformationProperty(kprop, att, format)
                            if (!dic.containsKey(property.Name)) {
                                properties.add(property)
                                dic[property.Name] = property
                            }
                        }
                    } else if (kprop.annotations.filterIsInstance<OpenTraceabilityExtensionElementsAttribute>()
                            .isNotEmpty()
                    ) {
                        extensionKDEs = p
                    } else if (kprop.annotations.filterIsInstance<OpenTraceabilityExtensionAttributesAttribute>()
                            .isNotEmpty()
                    ) {
                        extensionAttributes = p
                    }
                    properties.sortWith(compareBy({ it.SequenceOrder == null }, { it.SequenceOrder }))
                }
            }
        }
    }

    companion object {
        private val locker = ReentrantLock()
        private val xmlTypeInfos = mutableMapOf<KClass<*>, OTMappingTypeInformation>()
        private val jsonTypeInfos = mutableMapOf<KClass<*>, OTMappingTypeInformation>()
        private val masterDataXmlTypeInfos = mutableMapOf<KClass<*>, OTMappingTypeInformation>()
        private val masterDataJsonTypeInfos = mutableMapOf<KClass<*>, OTMappingTypeInformation>()

        fun getXmlTypeInfo(t: KClass<*>): OTMappingTypeInformation {
            if (!xmlTypeInfos.containsKey(t)) {
                locker.lock()
                if (!xmlTypeInfos.containsKey(t)) {
                    val typeInfo = OTMappingTypeInformation(t, EPCISDataFormat.XML)
                    xmlTypeInfos[t] = typeInfo
                }
                locker.unlock()
            }
            return xmlTypeInfos[t]!!
        }

        fun getJsonTypeInfo(t: KClass<*>): OTMappingTypeInformation {
            if (!jsonTypeInfos.containsKey(t)) {
                locker.lock()
                if (!jsonTypeInfos.containsKey(t)) {
                    val typeInfo = OTMappingTypeInformation(t, EPCISDataFormat.JSON)
                    jsonTypeInfos[t] = typeInfo
                }
                locker.unlock()
            }
            return jsonTypeInfos[t]!!
        }

        fun getMasterDataXmlTypeInfo(t: KClass<*>): OTMappingTypeInformation {
            if (!masterDataXmlTypeInfos.containsKey(t)) {
                locker.lock()
                if (!masterDataXmlTypeInfos.containsKey(t)) {
                    val typeInfo = OTMappingTypeInformation(t, EPCISDataFormat.XML, true)
                    masterDataXmlTypeInfos[t] = typeInfo
                }
                locker.unlock()
            }
            return masterDataXmlTypeInfos[t]!!
        }

        fun getMasterDataJsonTypeInfo(t: KClass<*>): OTMappingTypeInformation {
            if (!masterDataJsonTypeInfos.containsKey(t)) {
                locker.lock()
                if (!masterDataJsonTypeInfos.containsKey(t)) {
                    val typeInfo = OTMappingTypeInformation(t, EPCISDataFormat.JSON, true)
                    masterDataJsonTypeInfos[t] = typeInfo
                }
                locker.unlock()
            }
            return masterDataJsonTypeInfos[t]!!
        }
    }

    operator fun get(name: String): OTMappingTypeInformationProperty? = dic[name]
}
