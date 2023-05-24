package models.events
import models.identifiers.*
import utility.Measurement
import java.lang.reflect.Type
class EventProduct {
    //TODO: review this


    var EPC: EPC = EPC()
    var Quantity: Measurement? = null
    var Type: EventProductType = EventProductType()

    constructor(epc: EPC) {
        this.EPC = epc;
    }

}
