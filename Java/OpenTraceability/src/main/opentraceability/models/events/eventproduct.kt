package models.events

import models.identifiers.*
import models.identifiers.EPC
import utility.Measurement

class EventProduct {

    var EPC: EPC
    var Quantity: Measurement? = null
    lateinit var Type: EventProductType

    constructor(epc: EPC) {
        this.EPC = epc;
    }

}
