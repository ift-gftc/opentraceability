package opentraceability.models.masterdata


import opentraceability.interfaces.IMasterDataKDE
import opentraceability.interfaces.IVocabularyElement
import opentraceability.interfaces.VocabularyType
import opentraceability.models.common.LanguageString
import opentraceability.models.identifiers.*
import opentraceability.models.events.kdes.CertificationList
import opentraceability.models.identifiers.GLN
import opentraceability.models.identifiers.PGLN
import opentraceability.utility.attributes.OpenTraceabilityJsonAttribute
import opentraceability.utility.attributes.OpenTraceabilityMasterDataAttribute
import opentraceability.utility.attributes.OpenTraceabilityObjectAttribute
import java.util.*
import org.json.JSONObject

open class Location() : IVocabularyElement {

    override var id: String? = null

    override var epcisType: String? = "urn:epcglobal:epcis:vtype:Location"


    @OpenTraceabilityJsonAttribute("@type")
    override var jsonLDType: String? = "gs1:Place"

    override var vocabularyType: VocabularyType = VocabularyType.Location

    override var context: JSONObject? = null

    @OpenTraceabilityJsonAttribute("globalLocationNumber")
    var gln: GLN? = null


    @OpenTraceabilityJsonAttribute("cbvmda:owning_party")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:owning_party")
    var owningParty: PGLN? = null


    @OpenTraceabilityJsonAttribute("cbvmda:informationProvider")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#informationProvider")
    var informationProvider: PGLN? = null


    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityMasterDataAttribute("https://gs1.org/cbv/cbvmda:certificationList")
    var certificationList: CertificationList? = null


    @OpenTraceabilityJsonAttribute("name")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#name")
    var name: MutableList<LanguageString>? = null


    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityJsonAttribute("address")
    @OpenTraceabilityMasterDataAttribute
    var address: Address? = null

    @OpenTraceabilityJsonAttribute("cbvmda:unloadingPort")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#unloadingPort")
    var unloadingPort: String? = null

    override var kdes: MutableList<IMasterDataKDE> = mutableListOf()


}
