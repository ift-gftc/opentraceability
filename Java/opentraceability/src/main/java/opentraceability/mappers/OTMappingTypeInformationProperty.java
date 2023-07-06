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

    public String namespace = null;

    public String itemName;

    public Class itemType;

    public EPCISVersion version;
    public Integer sequenceOrder = -1;
    public String curieMapping;

    public OTMappingTypeInformationProperty(){}

    public OTMappingTypeInformationProperty(Field field, OpenTraceabilityMasterDataAttribute att, EPCISDataFormat format ) {

        this.field = field;

        List<OpenTraceabilityObjectAttribute> objectAttributes =
                ReflectionUtility.getFieldAnnotations(field, OpenTraceabilityObjectAttribute.class);
        this.isObject = !objectAttributes.isEmpty();

        List<OpenTraceabilityRepeatingAttribute> repeatingAttributes =
                ReflectionUtility.getFieldAnnotations(field, OpenTraceabilityRepeatingAttribute.class);

        this.isRepeating = !repeatingAttributes.isEmpty();

        this.name = att.name();

        OpenTraceabilityArrayAttribute arrayAttribute = ReflectionUtility.getFieldAnnotation(field, OpenTraceabilityArrayAttribute.class);

        if (arrayAttribute != null) {
            this.isArray = true;
            this.itemName = arrayAttribute.itemName();
            this.itemType = arrayAttribute.itemType();
        }
    }

    public OTMappingTypeInformationProperty(Field field, OpenTraceabilityAttribute att, EPCISDataFormat format ) {

        this.field = field;

        List<OpenTraceabilityObjectAttribute> objectAttributes = ReflectionUtility.getFieldAnnotations(field, OpenTraceabilityObjectAttribute.class);
        this.isObject = !objectAttributes.isEmpty();

        List<OpenTraceabilityRepeatingAttribute> repeatingAttributes = ReflectionUtility.getFieldAnnotations(field, OpenTraceabilityRepeatingAttribute.class);
        this.isRepeating = !repeatingAttributes.isEmpty();

        this.name = att.name();
        this.namespace = att.ns();
        this.version = att.version();
        this.sequenceOrder = att.sequenceOrder();

        OpenTraceabilityArrayAttribute arrayAttribute = ReflectionUtility.getFieldAnnotation(field, OpenTraceabilityArrayAttribute.class);

        if (arrayAttribute != null) {
            this.isArray = true;
            this.itemName = arrayAttribute.itemName();
            this.itemType = arrayAttribute.itemType();
        }

        if (format == EPCISDataFormat.JSON) {

            OpenTraceabilityJsonAttribute jsonAtt = ReflectionUtility.getFieldAnnotation(field, OpenTraceabilityJsonAttribute.class);

            if (jsonAtt != null) {
                this.name = jsonAtt.name();
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

        this.name = att.name();
        this.version = EPCISVersion.V2;

        OpenTraceabilityArrayAttribute arrayAttribute = ReflectionUtility.getFieldAnnotation(field, OpenTraceabilityArrayAttribute.class);

        if (arrayAttribute != null) {
            this.isArray = true;
            this.itemName = arrayAttribute.itemName();
            this.itemType = arrayAttribute.itemType();
        }

        if (format == EPCISDataFormat.JSON) {

            OpenTraceabilityJsonAttribute jsonAtt = ReflectionUtility.getFieldAnnotation(field, OpenTraceabilityJsonAttribute.class);

            if (jsonAtt != null) {
                this.name = jsonAtt.name();
            }
        }
    }

    public OTMappingTypeInformationProperty(
        Field field,
        OpenTraceabilityProductsAttribute att,
        EPCISDataFormat format
    )  {
        this.field = field;
        this.name = att.name();
        this.version = att.version();
        this.isEPCList = att.listType() == OpenTraceabilityProductsListType.EPCList;
        this.isQuantityList = att.listType() == OpenTraceabilityProductsListType.QuantityList;
        this.productType = att.productType();
        this.required = att.required();
        this.sequenceOrder = att.sequenceOrder();

        if (format == EPCISDataFormat.JSON) {

            OpenTraceabilityJsonAttribute jsonAtt = ReflectionUtility.getFieldAnnotation(field, OpenTraceabilityJsonAttribute.class);

            if (jsonAtt != null) {
                this.name = jsonAtt.name();
            }
        }
    }
}