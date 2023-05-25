package models.masterdata

import models.common.LanguageString
import utility.Country
import java.util.*
import java.lang.reflect.Type

class Address {

    //TODO: review this file

    //[OpenTraceabilityJson("@type")]
    var Type: String? = "gs1:PostalAddress"

    //[OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#streetAddressOne")]
    //[OpenTraceabilityJson("streetAddress")]
    var Address1: ArrayList<LanguageString>? = null

    //[OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#streetAddressTwo")]
    var Address2: ArrayList<LanguageString>? = null

    //[OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#city")]
    //[OpenTraceabilityJson("addressLocality")]
    var City: ArrayList<LanguageString>? = null

    //[OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#state")]
    //[OpenTraceabilityJson("addressRegion")]
    var State: ArrayList<LanguageString>? = null

    //[OpenTraceabilityMasterData("urn:epcglobal:cbv:mda#countryCode")]
    //[OpenTraceabilityJson("countyCode")]
    var Country: Country? = null
}
