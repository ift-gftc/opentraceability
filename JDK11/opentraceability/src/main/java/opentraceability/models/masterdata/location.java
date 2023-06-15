package opentraceability.models.masterdata;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONObject;
import opentraceability.interfaces.*;
import opentraceability.utility.attributes.*;
import opentraceability.models.common.LanguageString;
import opentraceability.models.events.kdes.CertificationList;
import opentraceability.models.identifiers.GLN;
import opentraceability.models.identifiers.PGLN;


public class Location implements IVocabularyElement {

    public Location(){
        epcisType = "urn:epcglobal:epcis:vtype:Location";
    }

    public String id = null;

    public String epcisType;

    @OpenTraceabilityJsonAttribute("@type")
    public String jsonLDType = "gs1:Place";

    public VocabularyType vocabularyType = VocabularyType.Location;

    public JSONObject context = null;

    @OpenTraceabilityJsonAttribute("globalLocationNumber")
    public GLN gln = null;

    @OpenTraceabilityJsonAttribute("cbvmda:owning_party")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:owning_party")
    public PGLN owningParty = null;

    @OpenTraceabilityJsonAttribute("cbvmda:informationProvider")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#informationProvider")
    public PGLN informationProvider = null;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityMasterDataAttribute("https://gs1.org/cbv/cbvmda:certificationList")
    public CertificationList certificationList = null;

    @OpenTraceabilityJsonAttribute("name")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#name")
    public List<LanguageString> name = new ArrayList<LanguageString>();

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityJsonAttribute("address")
    @OpenTraceabilityMasterDataAttribute
    public Address address = null;

    @OpenTraceabilityJsonAttribute("cbvmda:unloadingPort")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#unloadingPort")
    public String unloadingPort = null;

    public List<IMasterDataKDE> kdes = new ArrayList<IMasterDataKDE>();

}