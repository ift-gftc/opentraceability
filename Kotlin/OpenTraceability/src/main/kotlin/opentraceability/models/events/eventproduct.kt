package opentraceability.models.events

import opentraceability.models.identifiers.*
import opentraceability.utility.Measurement

class EventProduct {

    var EPC: EPC? = null
    var Quantity: Measurement? = null
    lateinit var Type: EventProductType

    constructor(){}

    constructor(epc: EPC) {
        this.EPC = epc;
    }

}
