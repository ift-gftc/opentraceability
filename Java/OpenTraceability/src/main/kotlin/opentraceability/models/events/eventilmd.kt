package models.events

import interfaces.IEventKDE
import java.util.*
import models.events.kdes.CertificationList
import Constants
import utility.attributes.*
import utility.Country
import java.time.OffsetDateTime

class EventILMD {

    @OpenTraceabilityAttribute(Constants.CBVMDA_NAMESPACE, "productionMethodForFishAndSeafoodCode")
    @OpenTraceabilityJsonAttribute("cbvmda:productionMethodForFishAndSeafoodCode")
    var ProductionMethodForFishAndSeafoodCode: String? = null

    @OpenTraceabilityAttribute(Constants.CBVMDA_NAMESPACE, "itemExpirationDate")
    @OpenTraceabilityJsonAttribute("cbvmda:itemExpirationDate")
    var ItemExpirationDate: OffsetDateTime? = null


    @OpenTraceabilityAttribute(Constants.CBVMDA_NAMESPACE, "productionDate")
    @OpenTraceabilityJsonAttribute("cbvmda:productionDate")
    var ProductionDate: OffsetDateTime? = null

    @OpenTraceabilityArrayAttribute
    @OpenTraceabilityRepeatingAttribute
    @OpenTraceabilityAttribute(Constants.CBVMDA_NAMESPACE, "countryOfOrigin")
    @OpenTraceabilityJsonAttribute("cbvmda:countryOfOrigin")
    var CountryOfOrigin: ArrayList<Country> = ArrayList<Country>()

    @OpenTraceabilityAttribute(Constants.CBVMDA_NAMESPACE, "lotNumber")
    @OpenTraceabilityJsonAttribute("cbvmda:lotNumber")
    var LotNumber: String? = null

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(Constants.CBVMDA_NAMESPACE, "certificationList")
    @OpenTraceabilityJsonAttribute("cbvmda:certificationList")
    var CertificationList: CertificationList? = null

    @OpenTraceabilityExtensionElementsAttribute
    var ExtensionKDEs: ArrayList<IEventKDE> = ArrayList<IEventKDE>()
}
