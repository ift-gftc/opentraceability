package opentraceability.mappers.epcis.xml;

import opentraceability.Setup;
import opentraceability.interfaces.IVocabularyElement;
import opentraceability.mappers.OTMappingTypeInformation;
import opentraceability.models.common.LanguageString;
import opentraceability.models.events.EPCISBaseDocument;
import opentraceability.models.masterdata.Location;
import opentraceability.models.masterdata.TradeItem;
import opentraceability.models.masterdata.TradingParty;
import opentraceability.models.masterdata.VocabularyElement;
import opentraceability.models.masterdata.kdes.MasterDataKDEObject;
import opentraceability.models.masterdata.kdes.MasterDataKDEString;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;
import opentraceability.utility.attributes.OpenTraceabilityObjectAttribute;
import opentraceability.utility.attributes.TrySetValueType;
import opentraceability.utility.attributes.XMLNodeInfo;
import opentraceability.utility.xml.*;
import opentraceability.utility.xml.elements.Element;
import opentraceability.utility.xml.elements.NodeList;

import org.w3c.dom.Node;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import kotlin.reflect.KClass;
import kotlin.reflect.KMutableProperty;
import kotlin.reflect.Type;
import kotlin.reflect.full.KClassifier;
import kotlin.reflect.full.createInstance;
import kotlin.reflect.full.memberProperties;
import kotlin.reflect.full.starProjectedType;

class EPCISXmlMasterDataReader {

    public static void ReadMasterData(EPCISBaseDocument doc, Element xMasterData) {
        Element xVocabList = xMasterData.element("VocabularyList");
        if (xVocabList != null) {
            for (Element xVocab : xVocabList.elements()) {
                String type = xVocab.getAttribute("type").toLowerCase();
                if (type != null) {
                    Element xVocabElementaryList = xVocab.element("VocabularyElementList");
                    if (xVocabElementaryList != null) {
                        for (Element xVocabElement : xVocabElementaryList.elements()) {
                            switch (type) {
                                case "urn:epcglobal:epcis:vtype:epcclass":
                                    ReadTradeitem(doc, xVocabElement, type);
                                    break;
                                case "urn:epcglobal:epcis:vtype:location":
                                    ReadLocation(doc, xVocabElement, type);
                                    break;
                                case "urn:epcglobal:epcis:vtype:party":
                                    ReadTradingParty(doc, xVocabElement, type);
                                    break;
                                default:
                                    ReadUnknown(doc, xVocabElement, type);
                                    break;
                            }
                        }
                    }
                }
            }
        }
    }

    public static void ReadTradeitem(EPCISBaseDocument doc, Element xTradeitem, String type) {
        String id = xTradeitem.getAttribute("id");
        TradeItem tradeitem = new TradeItem();
        tradeitem.gtin = new opentraceability.models.identifiers.GTIN(id);
        tradeitem.epcisType = type;

        // read the object
        ReadMasterDataObject(tradeitem, xTradeitem);
        doc.masterData.add(tradeitem);
    }

    public static void ReadLocation(EPCISBaseDocument doc, Element xLocation, String type) {
        String id = xLocation.getAttribute("id");
        KClass<? extends Location> t = Setup.MasterDataTypes.get(type);
        Location loc = t.createInstance();
        loc.gln = new opentraceability.models.identifiers.GLN(id);
        loc.epcisType = type;

        // read the object
        ReadMasterDataObject(loc, xLocation);
        doc.masterData.add(loc);
    }

    public static void ReadTradingParty(EPCISBaseDocument doc, Element xTradingParty, String type) {
        String id = xTradingParty.getAttribute("id");
        TradingParty tp = new TradingParty();
        tp.pgln = new opentraceability.models.identifiers.PGLN(id);
        tp.epcisType = type;

        // read the object
        ReadMasterDataObject(tp, xTradingParty);
        doc.masterData.add(tp);
    }

    public static void ReadUnknown(EPCISBaseDocument doc, Element xVocabElement, String type) {
        String id = xVocabElement.getAttribute("id");
        VocabularyElement ele = new VocabularyElement();
        ele.id = id;
        ele.epcisType = type;

        // read the object
        ReadMasterDataObject(ele, xVocabElement);
        doc.masterData.add(ele);
    }

    public static void ReadMasterDataObject(IVocabularyElement md, Element xMasterData) {
        ReadMasterDataObject(md, xMasterData, true);
    }

    public static void ReadMasterDataObject(IVocabularyElement md, Element xMasterData, boolean readKDEs) {
        OTMappingTypeInformation.VocabularyXmlTypeInfo mappedProperties
                = OTMappingTypeInformation.getMasterDataXmlTypeInfo((KClass) md.getClass());

        // work on expanded objects...
        // these are objects on the vocab element represented by one or more attributes in the EPCIS master data
        List<String> ignoreAttributes = new ArrayList<String>();
        for (OTMappingTypeInformation.XmlPropertyInfo property : mappedProperties.properties) {
            if (property.Name == "") {
                OTMappingTypeInformation.VocabularyXmlTypeInfo subMappedProperties = OTMappingTypeInformation.getMasterDataXmlTypeInfo((KClass) property.Property.getReturnType());
                boolean setAttribute = false;
                Object subObject = property.Property.getReturnType().createInstance();
                if (subObject != null) {

                    NodeList<Node> children = xMasterData.getChildNodes();
                    for (Node child : children) {
                        if (child.getNodeName().equalsIgnoreCase("attribute")) {
                            String id = XMLNodeInfo.getAttribute(child, "id");
                            OTMappingTypeInformation.XmlPropertyInfo propMapping = subMappedProperties.get(id);
                            if (propMapping != null) {
                                if (!TrySetValueType.invoke(child.getNodeValue(), property.Property, subObject)) {
                                    Object value = readKDEObject(child, propMapping.Property.getReturnType());
                                    try {
                                        propMapping.Property.getSetter().call(subObject, value);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                                setAttribute = true;
                                ignoreAttributes.add(id);
                            }
                        }
                    }

                    if (setAttribute) {
                        try {
                            property.Property.getSetter().call(md, subObject);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }

        // go through each standard attribute...
        NodeList<Node> children