package mappers

import com.sun.beans.introspect.PropertyInfo
import utility.attributes.*
import java.beans.BeanInfo
import java.util.*
import java.lang.reflect.Type
import kotlin.reflect.KProperty
import java.util.concurrent.locks.ReentrantLock

class OTMappingTypeInformation private constructor(
    val type: Class<*>,
    val properties: List<OTMappingTypeInformationProperty>,
    val extensionKDEs: PropertyInfo?,
    val extensionAttributes: PropertyInfo?
) {
    companion object {
        private val locker = ReentrantLock()
        private val xmlTypeInfos = mutableMapOf<Class<*>, OTMappingTypeInformation>()
        private val jsonTypeInfos = mutableMapOf<Class<*>, OTMappingTypeInformation>()
        private val masterDataXmlTypeInfos = mutableMapOf<Class<*>, OTMappingTypeInformation>()
        private val masterDataJsonTypeInfos = mutableMapOf<Class<*>, OTMappingTypeInformation>()

        fun getXmlTypeInfo(t: Class<*>): OTMappingTypeInformation {
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

        fun getJsonTypeInfo(t: Class<*>): OTMappingTypeInformation {
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

        fun getMasterDataXmlTypeInfo(t: Class<*>): OTMappingTypeInformation {
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

        fun getMasterDataJsonTypeInfo(t: Class<*>): OTMappingTypeInformation {
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

    private val dic = mutableMapOf<String, OTMappingTypeInformationProperty>()

    init {
        type.declaredProperties.forEach { p ->
            if (p.isAnnotationPresent(OpenTraceabilityXmlIgnoreAttribute::class.java) && format == EPCISDataFormat.XML) {
                return@forEach
            }

            if (isMasterDataMapping) {
                val mdAtt = p.getAnnotation(OpenTraceabilityMasterDataAttribute::class.java)

                if (mdAtt != null) {
                    val property = OTMappingTypeInformationProperty(p, mdAtt, format)
                    properties.add(property)
                    dic[property.name] = property
                }
            } else {
                val atts = p.getAnnotationsByType(OpenTraceabilityAttribute::class.java)
                val jsonAtt = p.getAnnotation(OpenTraceabilityJsonAttribute::class.java)
                val productAtts = p.getAnnotationsByType(OpenTraceabilityProductsAttribute::class.java)

                if (atts.isNotEmpty()) {
                    atts.forEach { att ->
                        val property = OTMappingTypeInformationProperty(p, att, format)
                        if (!dic.containsKey(property.name)) {
                            properties.add(property)
                            dic[property.name] = property
                        }
                    }
                } else if (jsonAtt != null && format == EPCISDataFormat.JSON) {
                    val property = OTMappingTypeInformationProperty(p, jsonAtt, format)
                    if (!dic.containsKey(property.name)) {
                        properties.add(property)
                        dic[property.name] = property
                    }
                } else if (productAtts.isNotEmpty()) {
                    productAtts.forEach { att ->
                        val property = OTMappingTypeInformationProperty(p, att, format)
                        if (!dic.containsKey(property.name)) {
                            properties.add(property)
                            dic[property.name] = property
                        }
                    }
                } else if (p.isAnnotationPresent(OpenTraceabilityExtensionElementsAttribute::class.java)) {
                    extensionKDEs = p
                } else if (p.isAnnotationPresent(OpenTraceabilityExtensionAttributesAttribute::class.java)) {
                    extensionAttributes = p
                }
                properties = properties.sortedWith(compareBy({ it.sequenceOrder == null }, { it.sequenceOrder })).toMutableList()
            }
        }
    }

    operator fun get(name: String): OTMappingTypeInformationProperty? = dic[name]
}
