package models.events

import models.identifiers.*
import utility.Measurement

class EventProduct {
    //TODO: review this


    var EPC: EPC
    var Quantity: Measurement? = null
    lateinit var Type: EventProductType

    constructor(epc: EPC) {
        this.EPC = epc;
    }

}
