package opentraceability.models.masterdata

import opentraceability.models.common.LanguageString
import opentraceability.utility.Country
import opentraceability.utility.attributes.OpenTraceabilityJsonAttribute
import opentraceability.utility.attributes.OpenTraceabilityMasterDataAttribute
import java.util.*

class Address {

    @OpenTraceabilityJsonAttribute("@type")
    var type: String? = "gs1:PostalAddress"

    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#streetAddressOne")
    @OpenTraceabilityJsonAttribute("streetAddress")
    var address1: MutableList<LanguageString>? = null

    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#streetAddressTwo")
    var address2: MutableList<LanguageString>? = null

    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#city")
    @OpenTraceabilityJsonAttribute("addressLocality")
    var city: MutableList<LanguageString>? = null

    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#state")
    @OpenTraceabilityJsonAttribute("addressRegion")
    var state: MutableList<LanguageString>? = null

    @OpenTraceabilityMasterDataAttribute("urn:epcglobal:cbv:mda#countryCode")
    @OpenTraceabilityJsonAttribute("countyCode")
    var country: Country? = null
}
