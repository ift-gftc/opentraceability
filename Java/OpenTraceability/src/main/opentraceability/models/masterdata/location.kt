package models.masterdata

import com.fasterxml.jackson.core.JsonToken
import interfaces.IMasterDataKDE
import interfaces.IVocabularyElement
import interfaces.VocabularyType
import models.common.LanguageString
import java.util.*
import models.identifiers.*
import models.events.kdes.CertificationList

//TODO: review this file

class Location /*: IVocabularyElement*/ {

    var ID: String? = null

    var EPCISType: String? = "urn:epcglobal:epcis:vtype:Location"


    //[OpenTraceabilityJson("@type")]
    var JsonLDType: String? = "gs1:Place"

    var VocabularyType: VocabularyType? = null

    var Context: JsonToken? = null

    //[OpenTraceabilityJson("globalLocationNumber")]
    var GLN: GLN? = null


    //[OpenTraceabilityJson("cbvmda:owning_party")]
    //[OpenTraceabilityMasterData("urn:epcglobal:cbv:owning_party")]
    var OwningParty: PGLN? = null


    //[OpenTraceabilityJson("cbvmda:informationProvider")]
    //[OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#informationProvider")]
    var InformationProvider: PGLN? = null


    //[OpenTraceabilityObject]
    //[OpenTraceabilityMasterData("https://gs1.org/cbv/cbvmda:certificationList")]
    var CertificationList: CertificationList? = null


    //[OpenTraceabilityJson("name")]
    //[OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#name")]
    var Name: List<LanguageString>? = null


    //[OpenTraceabilityObject]
    //[OpenTraceabilityJson("address")]
    //[OpenTraceabilityMasterData]
    var Address: Address? = null

    //[OpenTraceabilityJson("cbvmda:unloadingPort")]
    //[OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#unloadingPort")]
    var UnloadingPort: String? = null

    var KDEs: List<IMasterDataKDE> = ArrayList<IMasterDataKDE>()


}
