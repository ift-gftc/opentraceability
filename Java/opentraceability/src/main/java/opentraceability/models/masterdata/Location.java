package opentraceability.models.masterdata;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import opentraceability.models.identifiers.GTIN;
import org.json.JSONObject;
import opentraceability.interfaces.*;
import opentraceability.utility.attributes.*;
import opentraceability.models.common.LanguageString;
import opentraceability.models.events.kdes.CertificationList;
import opentraceability.models.identifiers.GLN;
import opentraceability.models.identifiers.PGLN;


public class Location extends IVocabularyElement {

    public Location()
    {
        epcisType = "urn:epcglobal:epcis:vtype:Location";
        vocabularyType = VocabularyType.Location;
        type = "gs1:Place";
    }

    public VocabularyType vocabularyType = VocabularyType.Location;

    public JSONObject context = null;

    @OpenTraceabilityJsonAttribute(name = "globalLocationNumber")
    public GLN gln = null;

    @Override
    public String getId() {
        return (gln == null) ? null : gln.toString();
    }

    @Override
    public void setId(String val) {
        gln = (val == null) ? null : new GLN(val);
    }

    @OpenTraceabilityJsonAttribute(name = "cbvmda:owning_party")
    @OpenTraceabilityMasterDataAttribute(name = "urn:epcglobal:cbv:owning_party")
    public PGLN owningParty = null;

    @OpenTraceabilityJsonAttribute(name = "cbvmda:informationProvider")
    @OpenTraceabilityMasterDataAttribute(name = "urn:epcglobal:cbv:mda#informationProvider")
    public PGLN informationProvider = null;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityMasterDataAttribute(name = "https://gs1.org/cbv/cbvmda:certificationList")
    public CertificationList certificationList = null;

    @OpenTraceabilityArrayAttribute(itemType = LanguageString.class)
    @OpenTraceabilityJsonAttribute(name = "name")
    @OpenTraceabilityMasterDataAttribute(name = "urn:epcglobal:cbv:mda#name")
    public List<LanguageString> name = new ArrayList<LanguageString>();

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityJsonAttribute(name = "address")
    @OpenTraceabilityMasterDataAttribute
    public Address address = null;

    @OpenTraceabilityJsonAttribute(name = "cbvmda:unloadingPort")
    @OpenTraceabilityMasterDataAttribute(name = "urn:epcglobal:cbv:mda#unloadingPort")
    public String unloadingPort = null;

    @OpenTraceabilityExtensionElementsAttribute
    public List<IMasterDataKDE> kdes = new ArrayList<>();
}