package opentraceability.models.masterdata;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import opentraceability.models.identifiers.GTIN;
import opentraceability.utility.attributes.*;
import org.json.JSONObject;
import opentraceability.interfaces.IMasterDataKDE;
import opentraceability.interfaces.IVocabularyElement;
import opentraceability.interfaces.VocabularyType;
import opentraceability.models.common.LanguageString;
import opentraceability.models.identifiers.PGLN;

public class TradingParty extends IVocabularyElement {

    public TradingParty()
    {
        epcisType = "urn:epcglobal:epcis:vtype:Party";
        vocabularyType = VocabularyType.TradingParty;
        type = "gs1:Organization";
    }

    @OpenTraceabilityJsonAttribute(name = "globalLocationNumber")
    public PGLN pgln;

    @Override
    public String getId() {
        return (pgln == null) ? null : pgln.toString();
    }

    @Override
    public void setId(String val) {
        pgln = (val == null) ? null : new PGLN(val);
    }

    @OpenTraceabilityJsonAttribute(name = "cbvmda:owning_party")
    @OpenTraceabilityMasterDataAttribute(name = "urn:epcglobal:cbv:owning_Party")
    public PGLN owningParty;

    @OpenTraceabilityJsonAttribute(name = "cbvmda:informationProvider")
    @OpenTraceabilityMasterDataAttribute(name = "urn:epcglobal:cbv:mda#informationProvider")
    public PGLN informationProvider;

    @OpenTraceabilityArrayAttribute(itemType = LanguageString.class)
    @OpenTraceabilityJsonAttribute(name = "organizationName")
    @OpenTraceabilityMasterDataAttribute(name = "urn:epcglobal:cbv:mda#name")
    public List<LanguageString> name;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityJsonAttribute(name = "address")
    public Address address;

    @OpenTraceabilityJsonAttribute(name = "gdst:iftp")
    @OpenTraceabilityMasterDataAttribute(name = "urn:gdst:kde#iftp")
    public String iftp;

    @OpenTraceabilityExtensionElementsAttribute
    public List<IMasterDataKDE> kdes = new ArrayList<>();
}