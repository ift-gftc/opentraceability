package opentraceability.models.masterdata

import opentraceability.interfaces.IMasterDataKDE
import opentraceability.interfaces.IVocabularyElement
import opentraceability.interfaces.VocabularyType
import opentraceability.models.common.LanguageString
import opentraceability.models.identifiers.GTIN
import opentraceability.models.identifiers.PGLN
import opentraceability.utility.attributes.OpenTraceabilityArrayAttribute
import opentraceability.utility.attributes.OpenTraceabilityJsonAttribute
import opentraceability.utility.attributes.OpenTraceabilityMasterDataAttribute
import java.util.*
import org.json.JSONObject

class TradeItem()  : IVocabularyElement {

    override var id: String? = null

    override var epcisType: String? = "urn:epcglobal:epcis:vtype:EPCClass"

    @OpenTraceabilityJsonAttribute("@type")
    override var jsonLDType: String? = "gs1:Product"

    override var vocabularyType: VocabularyType = VocabularyType.TradeItem

    override var context: JSONObject? = null

    @OpenTraceabilityJsonAttribute("gtin")
    var gtin: GTIN? = null

    @OpenTraceabilityJsonAttribute("productName")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#descriptionShort")
    var shortDescription: MutableList<LanguageString>? = mutableListOf()

    @OpenTraceabilityJsonAttribute("cbvmda:tradeItemConditionCode")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#tradeItemConditionCode")
    var tradeItemConditionCode: String? = null

    @OpenTraceabilityJsonAttribute("cbvmda:owning_party")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:owning_Party")
    var owningParty: PGLN? = null

    @OpenTraceabilityJsonAttribute("cbvmda:informationProvider")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#informationProvider")
    var informationProvider: PGLN? = null

    @OpenTraceabilityArrayAttribute
    @OpenTraceabilityJsonAttribute("cbvmda:speciesForFisheryStatisticsPurposesName")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#speciesForFisheryStatisticsPurposesName")
    var fisherySpeciesScientificName: MutableList<String>? = mutableListOf()

    @OpenTraceabilityArrayAttribute
    @OpenTraceabilityJsonAttribute("cbvmda:speciesForFisheryStatisticsPurposesCode")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#speciesForFisheryStatisticsPurposesCode")
    var fisherySpeciesCode: MutableList<String>? = mutableListOf()

    override var kdes: MutableList<IMasterDataKDE> = mutableListOf()
}
