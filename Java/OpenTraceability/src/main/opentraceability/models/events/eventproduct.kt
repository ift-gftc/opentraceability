package opentraceability.models.events

import opentraceability.models.identifiers.*
import opentraceability.models.identifiers.EPC
import opentraceability.utility.Measurement

class EventProduct {

    var EPC: EPC
    var Quantity: Measurement? = null
    lateinit var Type: EventProductType

    constructor(epc: EPC) {
        this.EPC = epc;
    }

}
