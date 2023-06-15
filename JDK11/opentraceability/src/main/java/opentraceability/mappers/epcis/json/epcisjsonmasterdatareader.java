package opentraceability.mappers.epcis.json;

import opentraceability.interfaces.IVocabularyElement;
import opentraceability.models.common.LanguageString;
import opentraceability.models.events.EPCISBaseDocument;
import opentraceability.models.masterdata.Location;
import opentraceability.models.masterdata.TradeItem;
import opentraceability.models.masterdata.TradingParty;
import opentraceability.models.masterdata.VocabularyElement;
import opentraceability.models.masterdata.kdes.MasterDataKDEObject;
import opentraceability.models.masterdata.kdes.MasterDataKDEString;
import opentraceability.models.identifiers.GTIN;
import opentraceability.models.identifiers.PGLN;
import opentraceability.utility.Countries;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;

public class EPCISJsonMasterDataReader {
    public static void readMasterData(EPCISBaseDocument doc, JSONObject jMasterData) {
        JSONArray jVocabList = jMasterData.optJSONArray("vocabularyList");
        if (jVocabList != null) {
            for (int i = 0; i < jVocabList.length(); i++) {
                JSONObject jVocabListItem = jVocabList.getJSONObject(i);
                String type = jVocabListItem.optString("type");
                JSONArray jVocabElementaryList = jVocabListItem.optJSONArray("vocabularyElementList");
                if (jVocabElementaryList != null) {
                    for (int j = 0; j < jVocabElementaryList.length(); j++) {
                        JSONObject jVocabEle = jVocabElementaryList.getJSONObject(j);
                        switch (type) {
                            case "urn:epcglobal:epcis:vtype:epcclass":
                                readTradeItem(doc, jVocabEle, type);
                                break;
                            case "urn:epcglobal:epcis:vtype:location":
                                readLocation(doc, jVocabEle, type);
                                break;
                            case "urn:epcglobal:epcis:vtype:party":
                                readTradingParty(doc, jVocabEle, type);
                                break;
                            default:
                                ReadUnknown(doc, jVocabEle, type);
                        }
                    }
                }
            }
        }
    }

    public static void readTradeItem(EPCISBaseDocument doc, JSONObject xTradeitem, String type) {
        String id = xTradeitem.optString("id", "");
        TradeItem tradeitem = new TradeItem();
        tradeitem.gtin = new GTIN(id);
        tradeitem.epcisType = type;

        readMasterDataObject(tradeitem, xTradeitem, true);
        doc.masterData.add(tradeitem);
    }

    public static void readLocation(EPCISBaseDocument doc, JSONObject xLocation, String type) {
        String id = xLocation.optString("id", "");
        Type t = opentraceability.Setup.MasterDataTypes.get(type);

        if (t != null) {
            Location loc = null;
            try {
                if (t.getClass() == Location.class) {
                    loc = Location.class.newInstance();
                } else {
                    throw new Exception("Failed to activate instance Location of " + t);
                }
            } catch (Exception e) {
                throw new RuntimeException();
            }
            loc.gln = new GLN(id);
            loc.epcisType = type;

            readMasterDataObject(loc, xLocation, true);
            doc.masterData.add(loc);
        }
    }

    public static void readTradingParty(EPCISBaseDocument doc, JSONObject xTradingParty, String type) throws Exception {
        String id = xTradingParty.optString("id", "");
        TradingParty tp = new TradingParty();
        tp.pgln = new PGLN(id);
        tp.epcisType = type;

        readMasterDataObject(tp, xTradingParty, true);
        doc.masterData.add(tp);
    }

    public static void ReadUnknown(EPCISBaseDocument doc, JSONObject xVocabElement, String type) {
        String id = xVocabElement.optString("id");
        VocabularyElement ele = new VocabularyElement();
        ele.id = id;
        ele.epcisType = type;

        readMasterDataObject(ele, xVocabElement, true);
        doc.masterData.add(ele);
    }

    public static void readMasterDataObject(IVocabularyElement md, JSONObject jMasterData, Boolean readKDEs) {
        ArrayList<String> ignoreAttributes = new ArrayList<>();

        for (OTMappingTypeInformation.PropertyInfo property : OTMappingTypeInformation.getMasterDataXmlTypeInfo(md.getClass()).getProperties().filter(e -> e.Name.isEmpty())) {
            try {
                OTMappingTypeInformation typeInfo = OTMappingTypeInformation.getMasterDataXmlTypeInfo(((TypeReference<Object>)property.Property.getReturnType()).getKotlinType());

                boolean setAttribute = false;
                //Object subObject = property.Property.getReturnType().getKotlinClass().create