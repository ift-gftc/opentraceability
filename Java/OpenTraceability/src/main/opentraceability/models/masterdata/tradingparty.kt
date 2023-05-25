package models.masterdata
import com.fasterxml.jackson.core.JsonToken
import interfaces.IMasterDataKDE
import interfaces.VocabularyType
import models.common.LanguageString
import java.util.*
import models.identifiers.*
import java.lang.reflect.Type


//TODO: review this file


class TradingParty/*: IVocabularyElement*/ {

    var ID: String? = null

    var EPCISType: String? = "urn:epcglobal:epcis:vtype:Party"


    //[OpenTraceabilityJson("@type")]
    var JsonLDType: String? = "gs1:Organization"

    var VocabularyType: VocabularyType? = null

    var Context: JsonToken? = null



    //[OpenTraceabilityJson("globalLocationNumber")]
    var PGLN: PGLN? = null

    //[OpenTraceabilityJson("cbvmda:owning_party")]
    //[OpenTraceabilityMasterData("urn:epcglobal:cbv:owning_Party")]
    var OwningParty: PGLN? = null


    //[OpenTraceabilityJson("cbvmda:informationProvider")]
    //[OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#informationProvider")]
    var InformationProvider: PGLN? = null



    //[OpenTraceabilityJson("organizationName")]
    //[OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#name")]
    var Name: ArrayList<LanguageString>? = null

    //[OpenTraceabilityObject]
    //[OpenTraceabilityJson("address")]
    var Address: Address? = null


    //[OpenTraceabilityJson("gdst:iftp")]
    //[OpenTraceabilityMasterData("urn:gdst:kde#iftp")]
    var IFTP: String? = null




    var KDEs: ArrayList<IMasterDataKDE> = ArrayList<IMasterDataKDE>()
}

