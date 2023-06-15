package opentraceability.mappers;

import opentraceability.models.events.*;
import opentraceability.utility.OTXmlUtil;
import opentraceability.utility.ReflectionUtility;
import opentraceability.utility.attributes.*;

import java.lang.reflect.Field;
import java.util.List;

public class OTMappingTypeInformationProperty {

    public Field field;
    public boolean required = false;
    public boolean isObject = false;
    public boolean isArray = false;
    public boolean isRepeating = false;
    public boolean isEPCList = false;
    public boolean isQuantityList = false;
    public EventProductType productType = EventProductType.Reference;

    public String name = "";
    public String itemName;
    public EPCISVersion version;
    public Integer sequenceOrder;
    public String curieMapping;

    public OTMappingTypeInformationProperty(){}

    public OTMappingTypeInformationProperty(Field property, OpenTraceabilityMasterDataAttribute att, EPCISDataFormat format ) {

        this.Property = property;

        List<OpenTraceabilityObjectAttribute> objectAttributes =
            OpentraceabilityMapperUtil.getAnnotationsByType(property, OpenTraceabilityObjectAttribute.class);
        this.IsObject = !objectAttributes.isEmpty();

        List<OpenTraceabilityRepeatingAttribute> repeatingAttributes =
            OpentraceabilityMapperUtil.getAnnotationsByType(property, OpenTraceabilityRepeatingAttribute.class);
        this.IsRepeating = !repeatingAttributes.isEmpty();

        this.Name = att.name;

        OpenTraceabilityArrayAttribute arrayAttribute =
            OpentraceabilityMapperUtil.getFirstAnnotationByType(property, OpenTraceabilityArrayAttribute.class);

        if (arrayAttribute != null) {
            this.IsArray = true;
            this.ItemName = arrayAttribute.itemName;
        }
    }

    public OTMappingTypeInformationProperty(Field field, OpenTraceabilityAttribute att, EPCISDataFormat format ) {

        this.field = field;

        List<OpenTraceabilityObjectAttribute> objectAttributes = ReflectionUtility.getFieldAnnotations(field, OpenTraceabilityObjectAttribute.class);
        this.isObject = !objectAttributes.isEmpty();

        List<OpenTraceabilityRepeatingAttribute> repeatingAttributes = ReflectionUtility.getFieldAnnotations(field, OpenTraceabilityRepeatingAttribute.class);
        this.isRepeating = !repeatingAttributes.isEmpty();

        this.name = att.name();
        this.version = att.version();
        this.sequenceOrder = att.sequenceOrder();

        OpenTraceabilityArrayAttribute arrayAttribute = ReflectionUtility.getFieldAnnotation(field, OpenTraceabilityArrayAttribute.class);

        if (arrayAttribute != null) {
            this.isArray = true;
            this.itemName = arrayAttribute.itemName;
        }

        if (format == EPCISDataFormat.JSON) {

            OpenTraceabilityJsonAttribute jsonAtt = ReflectionUtility.getFieldAnnotation(field, OpenTraceabilityJsonAttribute.class);

            if (jsonAtt != null) {
                this.name = jsonAtt.name;
            }
        }
    }

    public OTMappingTypeInformationProperty(
        Field property,
        OpenTraceabilityJsonAttribute att,
        EPCISDataFormat format
    ) {
        this.field = property;

        List<OpenTraceabilityObjectAttribute> objectAttributes =ReflectionUtility.getFieldAnnotations(field, OpenTraceabilityObjectAttribute.class);
        this.isObject = !objectAttributes.isEmpty();

        List<OpenTraceabilityRepeatingAttribute> repeatingAttributes = ReflectionUtility.getFieldAnnotations(field, OpenTraceabilityRepeatingAttribute.class);
        this.isRepeating = !repeatingAttributes.isEmpty();

        this.name = att.name;

        OpenTraceabilityArrayAttribute arrayAttribute = ReflectionUtility.getFieldAnnotation(field, OpenTraceabilityArrayAttribute.class);

        if (arrayAttribute != null) {
            this.isArray = true;
            this.itemName = arrayAttribute.itemName;
        }

        if (format == EPCISDataFormat.JSON) {

            OpenTraceabilityJsonAttribute jsonAtt = ReflectionUtility.getFieldAnnotation(field, OpenTraceabilityArrayAttribute.class);

            if (jsonAtt != null) {
                this.name = jsonAtt.name;
            }
        }
    }

    public OTMappingTypeInformationProperty(
        Field field,
        OpenTraceabilityProductsAttribute att,
        EPCISDataFormat format
    )  {
        this.field = field;
        this.name = att.name;
        this.version = att.version;
        this.sequenceOrder = att.sequenceOrder;
        this.isEPCList = att.listType == OpenTraceabilityProductsListType.EPCList;
        this.isQuantityList = att.listType == OpenTraceabilityProductsListType.QuantityList;
        this.productType = att.productType;
        this.required = att.required;

        if (format == EPCISDataFormat.JSON) {

            OpenTraceabilityJsonAttribute jsonAtt = ReflectionUtility.getFieldAnnotation(field, OpenTraceabilityJsonAttribute.class);

            if (jsonAtt != null) {
                this.name = jsonAtt.name;
            }
        }
    }
}