package models.events
import interfaces.IEventKDE
import java.util.*
import models.events.kdes.CertificationList
import utility.Country
import java.time.OffsetDateTime
class EventILMD {

    //TODO: review this

    //[OpenTraceability(Constants.CBVMDA_NAMESPACE, "productionMethodForFishAndSeafoodCode")]
    //[OpenTraceabilityJson("cbvmda:productionMethodForFishAndSeafoodCode")]
    var ProductionMethodForFishAndSeafoodCode: String? = null

    //[OpenTraceability(Constants.CBVMDA_NAMESPACE, "itemExpirationDate")]
    //[OpenTraceabilityJson("cbvmda:itemExpirationDate")]
    var ItemExpirationDate: OffsetDateTime? = null


    //[OpenTraceability(Constants.CBVMDA_NAMESPACE, "productionDate")]
    //[OpenTraceabilityJson("cbvmda:productionDate")]
    var ProductionDate: OffsetDateTime? = null

    //[OpenTraceabilityArray]
    //[OpenTraceabilityRepeatingAttribute]
    //[OpenTraceability(Constants.CBVMDA_NAMESPACE, "countryOfOrigin")]
    //[OpenTraceabilityJson("cbvmda:countryOfOrigin")]
    var CountryOfOrigin: ArrayList<Country> = ArrayList<Country>()

    //[OpenTraceability(Constants.CBVMDA_NAMESPACE, "lotNumber")]
    //[OpenTraceabilityJson("cbvmda:lotNumber")]
    var LotNumber: String? = null

    //[OpenTraceabilityObject]
    //[OpenTraceability(Constants.CBVMDA_NAMESPACE, "certificationList")]
    //[OpenTraceabilityJson("cbvmda:certificationList")]
    var CertificationList: CertificationList? = null

    //[OpenTraceabilityExtensionElements]
    var ExtensionKDEs: ArrayList<IEventKDE> = ArrayList<IEventKDE>()
}
