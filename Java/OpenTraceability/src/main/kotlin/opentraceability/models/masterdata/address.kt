package models.masterdata

import models.common.LanguageString
import utility.Country
import utility.attributes.OpenTraceabilityJsonAttribute
import utility.attributes.OpenTraceabilityMasterDataAttribute
import java.util.*
import java.lang.reflect.Type

class Address {

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
