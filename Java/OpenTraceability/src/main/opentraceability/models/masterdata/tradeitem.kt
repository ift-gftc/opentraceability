package models.masterdata

import com.fasterxml.jackson.core.JsonToken
import interfaces.IMasterDataKDE
import interfaces.VocabularyType
import models.common.LanguageString
import models.events.kdes.CertificationList
import java.util.*
import models.identifiers.*
import models.identifiers.GTIN
import models.identifiers.PGLN
import utility.attributes.OpenTraceabilityArrayAttribute
import utility.attributes.OpenTraceabilityJsonAttribute
import utility.attributes.OpenTraceabilityMasterDataAttribute
import java.lang.reflect.Type


class Tradeitem : IVocabularyElement {

    var ID: String? = null

    var EPCISType: String? = "urn:epcglobal:epcis:vtype:EPCClass"


    @OpenTraceabilityJsonAttribute("@type")
    var JsonLDType: String? = "gs1:Product"

    var VocabularyType: VocabularyType? = null

    var Context: JsonToken? = null

    @OpenTraceabilityJsonAttribute("gtin")
    var GTIN: GTIN? = null

    @OpenTraceabilityJsonAttribute("productName")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#descriptionShort")
    var ShortDescription: ArrayList<LanguageString>? = null

    @OpenTraceabilityJsonAttribute("cbvmda:tradeItemConditionCode")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#tradeItemConditionCode")
    var TradeItemConditionCode: String? = null

    @OpenTraceabilityJsonAttribute("cbvmda:owning_party")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:owning_Party")
    var OwningParty: PGLN? = null


    @OpenTraceabilityJsonAttribute("cbvmda:informationProvider")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#informationProvider")
    var InformationProvider: PGLN? = null


    @OpenTraceabilityArrayAttribute
    @OpenTraceabilityJsonAttribute("cbvmda:speciesForFisheryStatisticsPurposesName")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#speciesForFisheryStatisticsPurposesName")
    var FisherySpeciesScientificName: ArrayList<String>? = null


    @OpenTraceabilityArrayAttribute
    @OpenTraceabilityJsonAttribute("cbvmda:speciesForFisheryStatisticsPurposesCode")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#speciesForFisheryStatisticsPurposesCode")
    var FisherySpeciesCode: ArrayList<String>? = null


    var KDEs: ArrayList<IMasterDataKDE> = ArrayList<IMasterDataKDE>()
}
