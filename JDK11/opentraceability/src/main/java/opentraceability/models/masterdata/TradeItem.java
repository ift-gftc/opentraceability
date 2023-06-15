package opentraceability.models.masterdata;

import java.util.List;
import java.util.ArrayList;
import org.json.JSONObject;
import opentraceability.interfaces.IMasterDataKDE;
import opentraceability.interfaces.IVocabularyElement;
import opentraceability.interfaces.VocabularyType;
import opentraceability.models.common.LanguageString;
import opentraceability.models.identifiers.GTIN;
import opentraceability.models.identifiers.PGLN;
import opentraceability.utility.attributes.OpenTraceabilityArrayAttribute;
import opentraceability.utility.attributes.OpenTraceabilityJsonAttribute;
import opentraceability.utility.attributes.OpenTraceabilityMasterDataAttribute;

public class TradeItem extends IVocabularyElement {
    public String id = null;
    public String epcisType = "urn:epcglobal:epcis:vtype:EPCClass";
    @OpenTraceabilityJsonAttribute(name = "@type")
    public String jsonLDType = "gs1:Product";
    public VocabularyType vocabularyType = VocabularyType.TradeItem;
    public JSONObject context = null;
    @OpenTraceabilityJsonAttribute(name = "gtin")
    public GTIN gtin = null;
    @OpenTraceabilityJsonAttribute(name = "productName")
    @OpenTraceabilityMasterDataAttribute(name = "urn:epcglobal:cbv:mda#descriptionShort")
    public List<LanguageString> shortDescription = new ArrayList<LanguageString>();
    @OpenTraceabilityJsonAttribute(name = "cbvmda:tradeItemConditionCode")
    @OpenTraceabilityMasterDataAttribute(name = "urn:epcglobal:cbv:mda#tradeItemConditionCode")
    public String tradeItemConditionCode = null;
    @OpenTraceabilityJsonAttribute(name = "cbvmda:owning_party")
    @OpenTraceabilityMasterDataAttribute(name = "urn:epcglobal:cbv:owning_Party")
    public PGLN owningParty = null;
    @OpenTraceabilityJsonAttribute(name = "cbvmda:informationProvider")
    @OpenTraceabilityMasterDataAttribute(name = "urn:epcglobal:cbv:mda#informationProvider")
    public PGLN informationProvider = null;
    @OpenTraceabilityArrayAttribute
    @OpenTraceabilityJsonAttribute(name = "cbvmda:speciesForFisheryStatisticsPurposesName")
    @OpenTraceabilityMasterDataAttribute(name = "urn:epcglobal:cbv:mda#speciesForFisheryStatisticsPurposesName")
    public List<String> fisherySpeciesScientificName = new ArrayList<String>();
    @OpenTraceabilityArrayAttribute
    @OpenTraceabilityJsonAttribute(name = "cbvmda:speciesForFisheryStatisticsPurposesCode")
    @OpenTraceabilityMasterDataAttribute(name = "urn:epcglobal:cbv:mda#speciesForFisheryStatisticsPurposesCode")
    public List<String> fisherySpeciesCode = new ArrayList<String>();
}