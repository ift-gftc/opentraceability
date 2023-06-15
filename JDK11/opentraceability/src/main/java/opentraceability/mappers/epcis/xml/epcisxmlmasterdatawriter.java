package opentraceability.mappers.epcis.xml;

import opentraceability.interfaces.IMasterDataKDE;
import opentraceability.interfaces.IVocabularyElement;
import opentraceability.mappers.OTMappingTypeInformation;
import opentraceability.models.common.LanguageString;
import opentraceability.models.events.EPCISBaseDocument;
import opentraceability.utility.*;
import opentraceability.utility.attributes.*;
import org.w3c.dom.Element;
import kotlin.reflect.KClass;
import kotlin.reflect.full.KTypeProjection;
import kotlin.reflect.full.KotlinReflectionInternalError;
import kotlin.reflect.full.KotlinType;
import kotlin.reflect.full.starProjectedType;
import kotlin.reflect.typeOf;

import java.util.ArrayList;
import java.util.List;

public class EPCISXmlMasterDataWriter {
    public static void WriteMasterData(Element xDocument, EPCISBaseDocument doc) throws Exception {
        if (doc.masterData.size() > 0) {
            Element xEPCISHeader = xDocument.element("EPCISHeader");
            if (xEPCISHeader == null) {
                xDocument.addElement("EPCISHeader")
                        .addElement("extension")
                        .addElement("EPCISMasterData")
                        .addElement("VocabularyList");
            } else {
                xEPCISHeader.addElement("extension")
                        .addElement("EPCISMasterData")
                        .addElement("VocabularyList");
            }

            Element xVocabList = xDocument.getFirstElementByXPath("EPCISHeader/extension/EPCISMasterData/VocabularyList");
            if (xVocabList == null) {
                throw new Exception("Failed to grab the element EPCISHeader/extension/EPCISMasterData/VocabularyList");
            }

            for (IVocabularyElement v : doc.masterData.groupBy(IVocabularyElement::getEpcisType)
            .values()) {
                if (v.getEpcisType() != null) {
                    WriteMasterDataList(v, xVocabList, v.getEpcisType());
                } else {
                    throw new Exception("There are master data vocabulary elements where the Type is NULL.");
                }
            }
        }
    }

    public static void WriteMasterDataList(List<IVocabularyElement> data, Element xVocabList, String type) throws Exception {
        if (data.size() > 0) {
            Element xVocab = createXmlElement(("Vocabulary"));
            xVocab.setAttribute("type", type);
            Element xVocabEleList = xVocab.addElement("VocabularyElementList");

            for (IVocabularyElement md : data) {
                Element xMD = WriteMasterDataObject(md);
                xVocabEleList.addElement(xMD);
            }

            xVocabList.addElement(xVocab);
        }
    }

    public static Element WriteMasterDataObject(IVocabularyElement md) throws Exception {
        Element xVocabEle = OTXmlUtil.createXmlElement("VocabularyElement");
        xVocabEle.setAttribute("id", md.id);

        List<OTMappingTypeInformation> mappings = OTMappingTypeInformation.getMasterDataXmlTypeInfo(md.getClass());

        for (OTMappingTypeInformation.PropertyMapping mapping : mappings) {
            String id = mapping.Name;
            kotlin.reflect.KProperty1 p = mapping.Property;

            Object o = p.get(md);
            if (o != null) {
                if (id.isEmpty()) {
                    List<OTMappingTypeInformation.PropertyMapping> subMappings = null;
                    try {
                        subMappings = OTMappingTypeInformation.
                                getMasterDataXmlTypeInfo(o.getClass()
                                .asSubclass(Object.class)
                                .kotlin
                                .starProjectedType);
                    } catch (KotlinReflectionInternalError e) {
                        subMappings = OTMappingTypeInformation
                                .getMasterDataXmlTypeInfo(o.getClass());
                    }
                    for (OTMappingTypeInformation.PropertyMapping subMapping : subMappings) {
                        String subID = subMapping.Name;
                        kotlin.reflect.KProperty1<?> subProperty = subMapping.Property;
                        Object subObj = subProperty.get(o);
                        if (subObj != null) {
                            if (subObj.getClass().asSubclass(Object.class)
                                    .kotlin.starProjectedType == typeOf(ArrayList.class,
                                    new KTypeProjection(null, typeOf(LanguageString.class)))) {
                                ArrayList<LanguageString> l = (ArrayList<LanguageString>) subObj;
                                
                                String str = l.isEmpty() ? null : l.get(0).value;

                                if (str != null) {
                                    Element xAtt = xVocabEle.addElement("attribute");
                                    xAtt.nodeValue = str;
                                    xAtt.setAttribute("id", subID);
                                }
                            } else {
                                String str = subObj.toString();
                                if (!str.isEmpty()) {
                                    Element xAtt = xVocabEle.addElement("attribute");
                                    xAtt.nodeValue = str;
                                    xAtt.setAttribute("id", subID);
                                }
                            }
                        }
                    }
                } else if (p.isAnnotationPresent(OpenTraceabilityObjectAttribute.class).size() > 0) {
                    Element xAtt = xVocabEle.addElement("attribute");
                    xAtt.setAttribute("id", id);
                    WriteObject(xAtt, (Type) p.getReturnType(), o);
                } else if (p.getAnnotations().filterIsInstance(OpenTraceabilityArrayAttribute.class).size() > 0) {
                    ArrayList l = (ArrayList) o;
                    for (Object i : l) {
                        String str = i.toString();
                        if (!str.isEmpty()) {
                            Element xAtt = OTXmlUtil.addElement(xVocabEle, "attribute");
                            xAtt.setAttribute("id", id);
                            xAtt.setTextContent(str);
                        }
                    }
                } else if (o.getClass().asSubclass(Object.class)
                        .kotlin.starProjectedType == typeOf(ArrayList.class,
                        new KTypeProjection(null, typeOf(LanguageString.class)))) {
                    ArrayList<LanguageString> l = (ArrayList<LanguageString>) o;
                    String str = l.isEmpty() ? null : l.get(0).value;
                    if (str != null) {
                        Element xAtt = OTXmlUtil.addElement(xVocabEle, "attribute");
                        xAtt.setAttribute("id", id);
                        xAtt.setTextContent(str);
                    }
                } else {
                    String str = o.toString();
                    if (!str.isEmpty()) {
                        Element xAtt = OTXmlUtil.addElement(xVocabEle, "attribute");
                        xAtt.setAttribute("id", id);
                        xAtt.setTextContent(str);
                    }
                }
            }
        }

        for (IMasterDataKDE kde : md.kdes) {
            Element xKDE = kde.getEPCISXml();
            if (xKDE != null) {
                OTXmlUtil.addElement(xVocabEle, xKDE);
            }
        }

        return xVocabEle;
    }

    public static void WriteObject(Element x, Type t, Object o) throws Exception {
        for (kotlin.reflect.KProperty1 property : t.getMemberProperties()) {
            Object value = property.get(o);
            if (value != null) {
                OpenTraceabilityAttribute xmlAtt = property.getAnnotations()
                        .filterIsInstance(OpenTraceabilityAttribute.class)
                        .stream()
                        .findFirst().orElse(null);
                if (xmlAtt != null) {
                    Element xChild = x.addElementNS(xmlAtt.ns(), xmlAtt.name());
                    if (property.getAnnotations().filterIsInstance(OpenTraceabilityObjectAttribute.class).size() > 0) {
                        WriteObject(xChild, (Type) property.getReturnType(), value);
                    } else {
                        String str = value.toString();
                        if (str != null) {
                            xChild.nodeValue = str;
                        }
                    }
                }
            }
        }
    }
}