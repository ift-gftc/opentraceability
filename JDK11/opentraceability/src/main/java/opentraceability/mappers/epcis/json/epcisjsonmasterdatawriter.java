package opentraceability.mappers.epcis.json;

import opentraceability.interfaces.IVocabularyElement;
import opentraceability.mappers.OTMappingTypeInformation;
import opentraceability.models.common.LanguageString;
import opentraceability.models.events.EPCISBaseDocument;
import opentraceability.models.vocabulary.MasterDataVocabElement;
import opentraceability.utility.attributes.OpenTraceabilityArrayAttribute;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;
import opentraceability.utility.attributes.OpenTraceabilityObjectAttribute;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.lang.reflect.Type;
import kotlin.reflect.KClass;
import kotlin.reflect.full.KClassifier;
import kotlin.reflect.full.KTypeProjection;
import kotlin.reflect.full.KTypeProjection.Companion.STAR;
import kotlin.reflect.full.KTypeProjection.Companion.invariant;
import kotlin.reflect.full.KTypeParameter;
import kotlin.reflect.full.KTypeProjection.Companion.typeOf;
import kotlin.reflect.full.KTypeProjection.Companion.variance;
import kotlin.reflect.full.createType;
import kotlin.reflect.full.memberProperties;

public class EPCISJsonMasterDataWriter {
    public static void writeMasterData(JSONObject jDoc, EPCISBaseDocument doc) throws Exception {
        if (doc.masterData.size() > 0) {
            JSONObject xEPCISHeader = jDoc.optJSONObject("epcisHeader");
            if (xEPCISHeader == null) {
                xEPCISHeader = new JSONObject();
            }
            JSONObject epcisMasterData = xEPCISHeader.optJSONObject("epcisMasterData");
            if (epcisMasterData == null) {
                epcisMasterData = new JSONObject();
            }
            JSONArray vocabularyList = epcisMasterData.optJSONArray("vocabularyList");
            if (vocabularyList == null) {
                vocabularyList = new JSONArray();
            }

            for (IVocabularyElement vocabElement : doc.masterData) {
                if (vocabElement instanceof IVocabularyElement) {
                    IVocabularyElement mdVe = (IVocabularyElement) vocabElement;
                    JSONObject jVocabList = writeMasterDataList(mdVe.elements, mdVe.type);
                    vocabularyList.put(jVocabList);
                } else {
                    throw new Exception("There are master data vocabulary elements where the Type is NULL.");
                }
            }

            epcisMasterData.put("vocabularyList", vocabularyList);
            xEPCISHeader.put("epcisMasterData", epcisMasterData);
            jDoc.put("epcisHeader", xEPCISHeader);
        }
    }

    private static JSONObject writeMasterDataList(ArrayList<?> data, String type) throws Exception {
        JSONObject jVocab = new JSONObject();
        JSONArray vocabularyElementList = new JSONArray();

        for (Object obj : data) {
            if (obj instanceof IVocabularyElement) {
                IVocabularyElement ve = (IVocabularyElement) obj;
                JSONObject xMD = writeMasterDataObject(ve);
                vocabularyElementList.put(xMD);
            } else {
                throw new Exception("Unable to create JSONObject, Object is not IVocabularyElement.");
            }
        }

        jVocab.put("type", type);
        jVocab.put("vocabularyElementList", vocabularyElementList);

        return jVocab;
    }

    private static JSONObject writeMasterDataObject(IVocabularyElement md) throws Exception {
        JSONObject jVocabElement = new JSONObject();
        JSONArray attributes = new JSONArray();

        for (OTMappingTypeInformation.PropertyMapping mapping : OTMappingTypeInformation.getMasterDataXmlTypeInfo((Type) md.getClass())) {
            String id = mapping.Name;
            OTMappingTypeInformation.Property p = mapping.Property;

            Object o = p.getGetter().call(md);
            if (o != null) {
                if (id.isBlank()) {
                    for (OTMappingTypeInformation.PropertyMapping subMapping : OTMappingTypeInformation.getMasterDataXmlTypeInfo((Type) o.getClass())) {
                        String subID = subMapping.Name;
                        OTMappingTypeInformation.Property subProperty = subMapping.Property;
                        Object subObj = subProperty.getGetter().call(o);
                        if (subObj != null) {
                            if (subObj instanceof ArrayList<?>) {
                                ArrayList<LanguageString> l = (ArrayList<LanguageString>) subObj;
                                String str = l.size() > 0 ? l.get(0).getValue() : null;
                                if (str != null) {
                                    JSONObject jAttribute = new JSONObject();
                                    jAttribute.put("id", subID);
                                    jAttribute.put("attribute", str);
                                    attributes.put(jAttribute);
                                }
                            } else {
                                String str = subObj.toString();
                                if (!str.isBlank()) {
                                    JSONObject jAttribute = new JSONObject();
                                    jAttribute.put("id", subID);
                                    jAttribute.put("attribute", str);
                                    attributes.put(jAttribute);
                                }
                            }
                        }
                    }
                } else if (p.getAnnotations().filterIsInstance(OpenTraceabilityObjectAttribute.class).size() > 0) {
                    JSONObject jAttribute = new JSONObject();
                    jAttribute.put("id", id);
                    jAttribute.put("attribute", writeObject(p.getReturnType(), o));
                    attributes.put(jAttribute);
                } else if (p.getAnnotations().filterIsInstance(OpenTraceabilityArrayAttribute.class).size() > 0) {
                    ArrayList<?> l = (ArrayList<?>) o;
                    for (Object i : l) {
                        String str = i.toString();
                        if (!str.isBlank()) {
                            JSONObject jAttribute = new JSONObject();
                            jAttribute.put("id", id);
                            jAttribute.put("attribute", str);
                            attributes.put(jAttribute);
                        }
                    }
                } else if (o.getClass() == typeOf(ArrayList.class).getClassifier()) {
                    ArrayList<LanguageString> l = (ArrayList<LanguageString>) o;
                    String str = l.size() > 0 ? l.get(0).getValue() : null;
                    if (str != null) {
                        JSONObject jAttribute = new JSONObject();
                        jAttribute.put("id", id);
                        jAttribute.put("attribute", str);
                        attributes.put(jAttribute);
                    }
                } else {
                    String str = o.toString();
                    if (!str.isBlank()) {
                        JSONObject jAttribute = new JSONObject();
                        jAttribute.put("id", id);
                        jAttribute.put("attribute", str);
                        attributes.put(jAttribute);
                    }
                }
            }
        }

        for (OPCUAKD kp : md.kdes) {
            JSONObject jKDE = kp.getGS1WebVocabJson();
            if (jKDE != null) {
                JSONObject jAttribute = new JSONObject();
                jAttribute.put("id", kp.getName());
                jAttribute.put("attribute", jKDE);
                attributes.put(jAttribute);
            }
        }
        jVocabElement.put("id", md.getID() != null ? md.getID() : "");
        jVocabElement.put("attributes", attributes);

        return jVocabElement;
    }

    private static JSONObject writeObject(Type t, Object o) throws Exception {
        JSONObject j = new JSONObject();
        for (kotlin.reflect.Field property : ((Type)t).getMemberProperties()) {
            Object value = property.getter.call(o);
            if (value != null) {
                OpenTraceabilityAttribute xmlAtt = property.getter.getAnnotations().filterIsInstance(OpenTraceabilityAttribute.class).get(0);                
                if (xmlAtt != null) {
                    if (property.getter.getAnnotations().filterIsInstance(OpenTraceabilityObjectAttribute.class).size() > 0) {
                        j.put(xmlAtt.Name(), writeObject(property.getter.getReturnType(), value));
                    } else {
                        String str = value.toString();
                        if (!str.isBlank()) {
                            j.put(xmlAtt.Name(), str);
                        }
                    }
                }
            }
        }
        return j;
    }
}