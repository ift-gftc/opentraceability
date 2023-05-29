package opentraceability.models.masterdata
import com.fasterxml.jackson.core.JsonToken
import opentraceability.interfaces.IMasterDataKDE
import opentraceability.interfaces.VocabularyType
import opentraceability.models.common.LanguageString
import java.util.*
import opentraceability.models.identifiers.*
import opentraceability.models.identifiers.PGLN
import opentraceability.utility.attributes.OpenTraceabilityJsonAttribute
import opentraceability.utility.attributes.OpenTraceabilityMasterDataAttribute
import opentraceability.utility.attributes.OpenTraceabilityObjectAttribute
import java.lang.reflect.Type


//TODO: review this file


class TradingParty/*: IVocabularyElement*/ {

    var ID: String? = null

    var EPCISType: String? = "urn:epcglobal:epcis:vtype:Party"


    @OpenTraceabilityJsonAttribute("@type")
    var JsonLDType: String? = "gs1:Organization"

    var VocabularyType: VocabularyType? = null

    var Context: JsonToken? = null



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




    var KDEs: ArrayList<IMasterDataKDE> = ArrayList<IMasterDataKDE>()
}

