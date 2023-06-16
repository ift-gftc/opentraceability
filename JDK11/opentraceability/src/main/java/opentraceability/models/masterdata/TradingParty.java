package opentraceability.models.masterdata;

import java.util.List;
import java.util.UUID;
import org.json.JSONObject;
import opentraceability.interfaces.IMasterDataKDE;
import opentraceability.interfaces.IVocabularyElement;
import opentraceability.interfaces.VocabularyType;
import opentraceability.models.common.LanguageString;
import opentraceability.models.identifiers.PGLN;
import opentraceability.utility.attributes.OpenTraceabilityJsonAttribute;
import opentraceability.utility.attributes.OpenTraceabilityMasterDataAttribute;
import opentraceability.utility.attributes.OpenTraceabilityObjectAttribute;

public class TradingParty extends IVocabularyElement {

    public TradingParty()
    {
        epcisType = "urn:epcglobal:epcis:vtype:Party";
        vocabularyType = VocabularyType.TradingParty;
    }

    @OpenTraceabilityJsonAttribute(name = "globalLocationNumber")
    public PGLN pgln;

    @OpenTraceabilityJsonAttribute(name = "cbvmda:owning_party")
    @OpenTraceabilityMasterDataAttribute(name = "urn:epcglobal:cbv:owning_Party")
    public PGLN owningParty;

    @OpenTraceabilityJsonAttribute(name = "cbvmda:informationProvider")
    @OpenTraceabilityMasterDataAttribute(name = "urn:epcglobal:cbv:mda#informationProvider")
    public PGLN informationProvider;

    @OpenTraceabilityJsonAttribute(name = "organizationName")
    @OpenTraceabilityMasterDataAttribute(name = "urn:epcglobal:cbv:mda#name")
    public List<LanguageString> name;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityJsonAttribute(name = "address")
    public Address address;

    @OpenTraceabilityJsonAttribute(name = "gdst:iftp")
    @OpenTraceabilityMasterDataAttribute(name = "urn:gdst:kde#iftp")
    public String iftp;

    public List<IMasterDataKDE> kdes;
}