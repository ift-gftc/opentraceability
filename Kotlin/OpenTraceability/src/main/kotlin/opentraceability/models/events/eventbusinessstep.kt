package opentraceability.models.events

class EventBusinessStep {
    companion object {
        const val Receiving: String = "urn:epcglobal:cbv:bizstep:receiving"
        const val Shipping: String = "urn:epcglobal:cbv:bizstep:shipping"
        const val Storage: String = "urn:epcglobal:cbv:bizstep:storing"
        const val Fishing: String = "urn:gdst:bizstep:fishingevent"
        const val Commissioning: String = "urn:epcglobal:cbv:bizstep:commissioning"
        const val Commingling: String = "urn:gdst:bizstep:commingling"
        const val Sampling: String = "urn:gdst:bizstep:sampling"
        const val Freezing: String = "urn:gdst:bizstep:freezing"
        const val Landing: String = "urn:gdst:bizstep:landing"
        const val Feeding: String = "urn:gdst:bizstep:feeding"
        const val Hatching: String = "urn:gdst:bizstep:hatching"
        const val Temperature: String = "urn:gdst:bizstep:temperature"
        const val Packaging: String = "urn:gdst:bizstep:packaging"
        const val Transshipment: String = "urn:gdst:bizstep:transshipment"
        const val FarmHarvest: String = "urn:gdst:bizstep:farmharvest"

        const val ReceivingURI: String = "https://ref.gs1.org/cbv/BizStep-receiving"
        const val ShippingURI: String = "https://ref.gs1.org/cbv/BizStep-shipping"
        const val StorageURI: String = "https://ref.gs1.org/cbv/BizStep-storing"
        const val CommissioningURI: String = "https://ref.gs1.org/cbv/BizStep-commissioning"
    }
}
