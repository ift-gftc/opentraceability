package opentraceability.models.masterdata


import opentraceability.interfaces.IMasterDataKDE
import opentraceability.interfaces.IVocabularyElement
import opentraceability.interfaces.VocabularyType
import opentraceability.models.common.LanguageString
import opentraceability.models.identifiers.PGLN
import opentraceability.utility.attributes.OpenTraceabilityJsonAttribute
import opentraceability.utility.attributes.OpenTraceabilityMasterDataAttribute
import opentraceability.utility.attributes.OpenTraceabilityObjectAttribute
import java.util.*
import org.json.JSONObject


class TradingParty : IVocabularyElement {

    override var id: String? = null

    override var epcisType: String? = "urn:epcglobal:epcis:vtype:Party"

    @OpenTraceabilityJsonAttribute("@type")
    override var jsonLDType: String? = "gs1:Organization"

    override var vocabularyType: VocabularyType = VocabularyType.TradingParty

    override var context: JSONObject? = null

    @OpenTraceabilityJsonAttribute("globalLocationNumber")
    var pgln: PGLN? = null

    @OpenTraceabilityJsonAttribute("cbvmda:owning_party")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:owning_Party")
    var owningParty: PGLN? = null

    @OpenTraceabilityJsonAttribute("cbvmda:informationProvider")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#informationProvider")
    var informationProvider: PGLN? = null

    @OpenTraceabilityJsonAttribute("organizationName")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#name")
    var name: MutableList<LanguageString>? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityJsonAttribute("address")
    var address: Address? = null

    @OpenTraceabilityJsonAttribute("gdst:iftp")
    @OpenTraceabilityMasterDataAttribute("urn:gdst:kde#iftp")
    var iftp: String? = null

    override var kdes: MutableList<IMasterDataKDE> = mutableListOf()
}

