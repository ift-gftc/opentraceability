package models.masterdata


import interfaces.IMasterDataKDE
import interfaces.IVocabularyElement
import interfaces.VocabularyType
import models.common.LanguageString
import java.util.*
import models.identifiers.*
import models.events.kdes.CertificationList
import models.identifiers.GLN
import models.identifiers.PGLN
import utility.attributes.OpenTraceabilityJsonAttribute
import utility.attributes.OpenTraceabilityMasterDataAttribute
import utility.attributes.OpenTraceabilityObjectAttribute

class Location : IVocabularyElement {

    var ID: String? = null

    var EPCISType: String? = "urn:epcglobal:epcis:vtype:Location"


    @OpenTraceabilityJsonAttribute("@type")
    var JsonLDType: String? = "gs1:Place"

    var VocabularyType: VocabularyType? = null

    var Context: JSONObject? = null

    @OpenTraceabilityJsonAttribute("globalLocationNumber")
    var GLN: GLN? = null


    @OpenTraceabilityJsonAttribute("cbvmda:owning_party")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:owning_party")
    var OwningParty: PGLN? = null


    @OpenTraceabilityJsonAttribute("cbvmda:informationProvider")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#informationProvider")
    var InformationProvider: PGLN? = null


    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityMasterDataAttribute("https://gs1.org/cbv/cbvmda:certificationList")
    var CertificationList: CertificationList? = null


    @OpenTraceabilityJsonAttribute("name")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#name")
    var Name: ArrayList<LanguageString>? = null


    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityJsonAttribute("address")
    @OpenTraceabilityMasterDataAttribute
    var Address: Address? = null

    @OpenTraceabilityJsonAttribute("cbvmda:unloadingPort")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#unloadingPort")
    var UnloadingPort: String? = null

    var KDEs: ArrayList<IMasterDataKDE> = ArrayList<IMasterDataKDE>()


}
