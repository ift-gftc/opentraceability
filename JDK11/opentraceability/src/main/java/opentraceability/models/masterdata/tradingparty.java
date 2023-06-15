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

public class TradingParty implements IVocabularyElement {
    public String id;
    public String epcisType = "urn:epcglobal:epcis:vtype:Party";
    public String jsonLDType = "gs1:Organization";
    public VocabularyType vocabularyType = VocabularyType.TradingParty;
    public JSONObject context;

    @OpenTraceabilityJsonAttribute("@id")
    public String getID() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    @OpenTraceabilityJsonAttribute("@id")
    public void setID(String value) {
        id = value;
    }

    @OpenTraceabilityJsonAttribute("globalLocationNumber")
    public PGLN pgln;

    @OpenTraceabilityJsonAttribute("cbvmda:owning_party")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:owning_Party")
    public PGLN owningParty;

    @OpenTraceabilityJsonAttribute("cbvmda:informationProvider")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#informationProvider")
    public PGLN informationProvider;

    @OpenTraceabilityJsonAttribute("organizationName")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#name")
    public List<LanguageString> name;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityJsonAttribute("address")
    public Address address;

    @OpenTraceabilityJsonAttribute("gdst:iftp")
    @OpenTraceabilityMasterDataAttribute("urn:gdst:kde#iftp")
    public String iftp;

    public List<IMasterDataKDE> kdes;
}