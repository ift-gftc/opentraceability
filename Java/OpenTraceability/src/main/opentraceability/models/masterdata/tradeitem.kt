package opentraceability.models.masterdata

import com.fasterxml.jackson.core.JsonToken
import opentraceability.interfaces.IMasterDataKDE
import opentraceability.interfaces.VocabularyType
import opentraceability.models.common.LanguageString
import opentraceability.models.events.kdes.CertificationList
import java.util.*
import opentraceability.models.identifiers.*
import opentraceability.models.identifiers.GTIN
import opentraceability.models.identifiers.PGLN
import opentraceability.utility.attributes.OpenTraceabilityArrayAttribute
import opentraceability.utility.attributes.OpenTraceabilityJsonAttribute
import opentraceability.utility.attributes.OpenTraceabilityMasterDataAttribute
import java.lang.reflect.Type

//TODO: review this file

class Tradeitem /*: IVocabularyElement*/ {

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
