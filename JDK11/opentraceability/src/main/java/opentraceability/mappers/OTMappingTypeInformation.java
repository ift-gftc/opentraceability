package opentraceability.mappers;

import opentraceability.utility.ReflectionUtility;
import opentraceability.utility.attributes.*;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class OTMappingTypeInformation {
    public java.lang.reflect.Type Type;
    public ArrayList<OTMappingTypeInformationProperty> properties = new ArrayList<>();
    public Map<String, OTMappingTypeInformationProperty> dic = new HashMap<>();
    public Field extensionKDEs = null;
    public Field extensionAttributes = null;

    public OTMappingTypeInformation() { }

    public OTMappingTypeInformation(Type type, EPCISDataFormat format, Boolean isMasterDataMapping) {
        Type = type;

        for (Field prop : type.getClass().getFields()) {
            if (prop != null) {
                if (format == EPCISDataFormat.XML && ReflectionUtility.getFieldAnnotation(prop, OpenTraceabilityXmlIgnoreAttribute.class) != null) {
                    continue;
                }

                if (isMasterDataMapping) {
                    List<OpenTraceabilityMasterDataAttribute> mdAtt = ReflectionUtility.getFieldAnnotations(prop, OpenTraceabilityMasterDataAttribute.class);
                    if (mdAtt != null && mdAtt.size() > 0) {
                        OTMappingTypeInformationProperty property = new OTMappingTypeInformationProperty(prop, mdAtt.get(0), format);
                        this.properties.add(property);
                        dic.put(property.name, property);
                    }
                } else {
                    List<OpenTraceabilityAttribute> atts = ReflectionUtility.getFieldAnnotations(prop, OpenTraceabilityAttribute.class);
                    List<OpenTraceabilityJsonAttribute> jsonAtt = ReflectionUtility.getFieldAnnotations(prop, OpenTraceabilityJsonAttribute.class);
                    List<OpenTraceabilityProductsAttribute> productAtts = ReflectionUtility.getFieldAnnotations(prop, OpenTraceabilityProductsAttribute.class);

                    if (atts.size() > 0) {
                        for (OpenTraceabilityAttribute att : atts) {
                            OTMappingTypeInformationProperty property = new OTMappingTypeInformationProperty(prop, att, format);
                            if (!dic.containsKey(property.name)) {
                                this.properties.add(property);
                                dic.put(property.name, property);
                            }
                        }
                    } else if (jsonAtt != null && jsonAtt.size() > 0 && format == EPCISDataFormat.JSON) {
                        OTMappingTypeInformationProperty property = new OTMappingTypeInformationProperty(prop, jsonAtt.get(0), format);
                        if (!dic.containsKey(property.name)) {
                            this.properties.add(property);
                            dic.put(property.name, property);
                        }
                    } else if (productAtts.size() > 0) {
                        for (OpenTraceabilityProductsAttribute att : productAtts) {
                            OTMappingTypeInformationProperty property = new OTMappingTypeInformationProperty(prop, att, format);
                            if (!dic.containsKey(property.name)) {
                                this.properties.add(property);
                                dic.put(property.name, property);
                            }
                        }
                    }
                    else if (ReflectionUtility.getFieldAnnotation(prop, OpenTraceabilityExtensionElementsAttribute.class) != null)
                    {
                        extensionKDEs = prop;
                    }
                    else if (ReflectionUtility.getFieldAnnotation(prop, OpenTraceabilityExtensionAttributesAttribute.class) != null)
                    {
                        extensionAttributes = prop;
                    }

                    properties.sort((a, b) -> Boolean.compare(a.sequenceOrder == null, b.sequenceOrder == null) != 0
                            ? a.sequenceOrder == null ? 1 : -1
                            : a.sequenceOrder - b.sequenceOrder);
                }
            }
        }
    }

    private static final ReentrantLock locker = new ReentrantLock();
    private static final Map<Type, OTMappingTypeInformation> xmlTypeInfos = new HashMap<>();
    private static final Map<Type, OTMappingTypeInformation> jsonTypeInfos = new HashMap<>();
    private static final Map<Type, OTMappingTypeInformation> masterDataXmlTypeInfos = new HashMap<>();
    private static final Map<Type, OTMappingTypeInformation> masterDataJsonTypeInfos = new HashMap<>();

    public static OTMappingTypeInformation getXmlTypeInfo(Type t) {
        if (!xmlTypeInfos.containsKey(t)) {
            locker.lock();
            if (!xmlTypeInfos.containsKey(t)) {
                OTMappingTypeInformation typeInfo = new OTMappingTypeInformation(t, EPCISDataFormat.XML, false);
                xmlTypeInfos.put(t, typeInfo);
            }
            locker.unlock();
        }
        return xmlTypeInfos.get(t);
    }

    public static OTMappingTypeInformation getJsonTypeInfo(Type t) {
        if (!jsonTypeInfos.containsKey(t)) {
            locker.lock();
            if (!jsonTypeInfos.containsKey(t)) {
                OTMappingTypeInformation typeInfo = new OTMappingTypeInformation(t, EPCISDataFormat.JSON, false);
                jsonTypeInfos.put(t, typeInfo);
            }
            locker.unlock();
        }
        return jsonTypeInfos.get(t);
    }

    public static OTMappingTypeInformation getMasterDataXmlTypeInfo(Type t) {
        if (!masterDataXmlTypeInfos.containsKey(t)) {
            locker.lock();
            if (!masterDataXmlTypeInfos.containsKey(t)) {
                OTMappingTypeInformation typeInfo = new OTMappingTypeInformation(t, EPCISDataFormat.XML, true);
                masterDataXmlTypeInfos.put(t, typeInfo);
            }
            locker.unlock();
        }
        return masterDataXmlTypeInfos.get(t);
    }

    public static OTMappingTypeInformation getMasterDataJsonTypeInfo(Type t) {
        if (!masterDataJsonTypeInfos.containsKey(t)) {
            locker.lock();
            if (!masterDataJsonTypeInfos.containsKey(t)) {
                OTMappingTypeInformation typeInfo = new OTMappingTypeInformation(t, EPCISDataFormat.JSON, true);
                masterDataJsonTypeInfos.put(t, typeInfo);
            }
            locker.unlock();
        }
        return masterDataJsonTypeInfos.get(t);
    }

    public OTMappingTypeInformationProperty get(String name) {
        return dic.get(name);
    }
}