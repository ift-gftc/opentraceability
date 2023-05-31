package models.masterdata


import interfaces.IMasterDataKDE
import interfaces.IVocabularyElement
import interfaces.VocabularyType
import models.common.LanguageString
import java.util.*
import models.identifiers.*
import models.identifiers.PGLN
import org.json.JSONObject
import utility.attributes.OpenTraceabilityJsonAttribute
import utility.attributes.OpenTraceabilityMasterDataAttribute
import utility.attributes.OpenTraceabilityObjectAttribute
import java.lang.reflect.Type


class TradingParty : IVocabularyElement {

    override var ID: String? = null

    override  var EPCISType: String? = "urn:epcglobal:epcis:vtype:Party"


    @OpenTraceabilityJsonAttribute("@type")
    override  var JsonLDType: String? = "gs1:Organization"

    override lateinit var VocabularyType: VocabularyType

    override  var Context: JSONObject? = null


    @OpenTraceabilityJsonAttribute("globalLocationNumber")
    var PGLN: PGLN? = null

    @OpenTraceabilityJsonAttribute("cbvmda:owning_party")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:owning_Party")
    var OwningParty: PGLN? = null


    @OpenTraceabilityJsonAttribute("cbvmda:informationProvider")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#informationProvider")
    var InformationProvider: PGLN? = null


    @OpenTraceabilityJsonAttribute("organizationName")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#name")
    var Name: ArrayList<LanguageString>? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityJsonAttribute("address")
    var Address: Address? = null


    @OpenTraceabilityJsonAttribute("gdst:iftp")
    @OpenTraceabilityMasterDataAttribute("urn:gdst:kde#iftp")
    var IFTP: String? = null


    override  var KDEs: ArrayList<IMasterDataKDE> = ArrayList<IMasterDataKDE>()
}

