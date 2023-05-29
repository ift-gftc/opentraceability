package opentraceability.models.masterdata

import com.fasterxml.jackson.core.JsonToken
import opentraceability.interfaces.IMasterDataKDE
import opentraceability.interfaces.IVocabularyElement
import opentraceability.interfaces.VocabularyType
import opentraceability.models.common.LanguageString
import java.util.*
import opentraceability.models.identifiers.*
import opentraceability.models.events.kdes.CertificationList
import opentraceability.models.identifiers.GLN
import opentraceability.models.identifiers.PGLN
import opentraceability.utility.attributes.OpenTraceabilityJsonAttribute
import opentraceability.utility.attributes.OpenTraceabilityMasterDataAttribute
import opentraceability.utility.attributes.OpenTraceabilityObjectAttribute

//TODO: review this file

class Location /*: IVocabularyElement*/ {

    var ID: String? = null

    var EPCISType: String? = "urn:epcglobal:epcis:vtype:Location"


    @OpenTraceabilityJsonAttribute("@type")
    var JsonLDType: String? = "gs1:Place"

    var VocabularyType: VocabularyType? = null

    var Context: JsonToken? = null

    @OpenTraceabilityJsonAttribute("globalLocationNumber")
    var GLN: GLN? = null


    @OpenTraceabilityJsonAttribute("cbvmda:owning_party")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:owning_party")
    var OwningParty: PGLN? = null


    @OpenTraceabilityJsonAttribute("cbvmda:informationProvider")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#informationProvider")
    var InformationProvider: PGLN? = null


    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityMasterDataAttribute("https://gs1.org/cbv/cbvmda:certificationList")
    var CertificationList: CertificationList? = null


    @OpenTraceabilityJsonAttribute("name")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#name")
    var Name: ArrayList<LanguageString>? = null


    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityJsonAttribute("address")
    @OpenTraceabilityMasterDataAttribute
    var Address: Address? = null

    @OpenTraceabilityJsonAttribute("cbvmda:unloadingPort")
    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#unloadingPort")
    var UnloadingPort: String? = null

    var KDEs: ArrayList<IMasterDataKDE> = ArrayList<IMasterDataKDE>()


}
