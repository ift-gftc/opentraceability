package opentraceability.mappers;

import opentraceability.utility.attributes.*;
import kotlin.reflect.*;
import kotlin.reflect.full.KMutableProperty;
import kotlin.reflect.full.memberProperties;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
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
                if (format == EPCISDataFormat.XML && prop.getAnnotations().first(OpenTraceabilityXmlIgnoreAttribute.class)
                        .size() > 0) {
                    continue;
                }

                if (isMasterDataMapping) {
                    OpenTraceabilityMasterDataAttribute[] mdAtt = prop.getAnnotations().filterIsInstance(OpenTraceabilityMasterDataAttribute.class).toArray(new OpenTraceabilityMasterDataAttribute[0]);
                    if (mdAtt != null && mdAtt.length > 0) {
                        OTMappingTypeInformationProperty property = new OTMappingTypeInformationProperty(prop, mdAtt[0], format);
                        this.properties.add(property);
                        dic.put(property.Name, property);
                    }
                } else {
                    OpenTraceabilityAttribute[] atts = prop.getAnnotations().filterIsInstance(OpenTraceabilityAttribute.class).toArray(new OpenTraceabilityAttribute[0]);
                    OpenTraceabilityJsonAttribute[] jsonAtt = prop.getAnnotations().filterIsInstance(OpenTraceabilityJsonAttribute.class).toArray(new OpenTraceabilityJsonAttribute[0]);
                    OpenTraceabilityProductsAttribute[] productAtts = prop.getAnnotations().filterIsInstance(OpenTraceabilityProductsAttribute.class).toArray(new OpenTraceabilityProductsAttribute[0]);

                    if (atts.length > 0) {
                        for (OpenTraceabilityAttribute att : atts) {
                            OTMappingTypeInformationProperty property = new OTMappingTypeInformationProperty(prop, att, format);
                            if (!dic.containsKey(property.Name)) {
                                this.properties.add(property);
                                dic.put(property.Name, property);
                            }
                        }
                    } else if (jsonAtt != null && jsonAtt.length > 0 && format == EPCISDataFormat.JSON) {
                        OTMappingTypeInformationProperty property = new OTMappingTypeInformationProperty(prop, jsonAtt[0], format);
                        if (!dic.containsKey(property.Name)) {
                            this.properties.add(property);
                            dic.put(property.Name, property);
                        }
                    } else if (productAtts.length > 0) {
                        for (OpenTraceabilityProductsAttribute att : productAtts) {
                            OTMappingTypeInformationProperty property = new OTMappingTypeInformationProperty(prop, att, format);
                            if (!dic.containsKey(property.Name)) {
                                this.properties.add(property);
                                dic.put(property.Name, property);
                            }
                        }
                    } else if (prop.getAnnotations().f(OpenTraceabilityExtensionElementsAttribute.class)
                            .size() > 0) {
                        extensionKDEs = prop;
                    } else if (prop.getAnnotations().filterIsInstance(OpenTraceabilityExtensionAttributesAttribute.class)
                            .size() > 0) {
                        extensionAttributes = prop;
                    }

                    properties.sort((a, b) -> Boolean.compare(a.SequenceOrder == null, b.SequenceOrder == null) != 0
                            ? a.SequenceOrder == null ? 1 : -1
                            : a.SequenceOrder - b.SequenceOrder);
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