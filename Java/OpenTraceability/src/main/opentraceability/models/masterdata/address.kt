package opentraceability.models.masterdata

import opentraceability.models.common.LanguageString
import opentraceability.utility.Country
import opentraceability.utility.attributes.OpenTraceabilityJsonAttribute
import opentraceability.utility.attributes.OpenTraceabilityMasterDataAttribute
import java.util.*
import java.lang.reflect.Type

class Address {

    //TODO: review this file

    @OpenTraceabilityJsonAttribute("@type")
    var Type: String? = "gs1:PostalAddress"

    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#streetAddressOne")
    @OpenTraceabilityJsonAttribute("streetAddress")
    var Address1: ArrayList<LanguageString>? = null

    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#streetAddressTwo")
    var Address2: ArrayList<LanguageString>? = null

    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#city")
    @OpenTraceabilityJsonAttribute("addressLocality")
    var City: ArrayList<LanguageString>? = null

    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#state")
    @OpenTraceabilityJsonAttribute("addressRegion")
    var State: ArrayList<LanguageString>? = null

    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#countryCode")
    @OpenTraceabilityJsonAttribute("countyCode")
    var Country: Country? = null
}
