package models.masterdata

import com.fasterxml.jackson.core.JsonToken
import interfaces.IMasterDataKDE
import interfaces.VocabularyType
import models.common.LanguageString
import models.events.kdes.CertificationList
import java.util.*
import models.identifiers.*
import java.lang.reflect.Type

//TODO: review this file

class Tradeitem /*: IVocabularyElement*/ {

    var ID: String? = null

    var EPCISType: String? = "urn:epcglobal:epcis:vtype:EPCClass"


    //[OpenTraceabilityJson("@type")]
    var JsonLDType: String? = "gs1:Product"

    var VocabularyType: VocabularyType? = null

    var Context: JsonToken? = null

    //[OpenTraceabilityJson("gtin")]
    var GTIN: GTIN? = null

    //[OpenTraceabilityJson("productName")]
    //[OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#descriptionShort")]
    var ShortDescription: ArrayList<LanguageString>? = null

    //[OpenTraceabilityJson("cbvmda:tradeItemConditionCode")]
    //[OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#tradeItemConditionCode")]
    var TradeItemConditionCode: String? = null

    //[OpenTraceabilityJson("cbvmda:owning_party")]
    //[OpenTraceabilityMasterData("urn:epcglobal:cbv:owning_Party")]
    var OwningParty: PGLN? = null


    //[OpenTraceabilityJson("cbvmda:informationProvider")]
    //[OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#informationProvider")]
    var InformationProvider: PGLN? = null


    //[OpenTraceabilityArray]
    //[OpenTraceabilityJson("cbvmda:speciesForFisheryStatisticsPurposesName")]
    //[OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#speciesForFisheryStatisticsPurposesName")]
    var FisherySpeciesScientificName: ArrayList<String>? = null


    //[OpenTraceabilityArray]
    //[OpenTraceabilityJson("cbvmda:speciesForFisheryStatisticsPurposesCode")]
    //[OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#speciesForFisheryStatisticsPurposesCode")]
    var FisherySpeciesCode: ArrayList<String>? = null


    var KDEs: ArrayList<IMasterDataKDE> = ArrayList<IMasterDataKDE>()
}
