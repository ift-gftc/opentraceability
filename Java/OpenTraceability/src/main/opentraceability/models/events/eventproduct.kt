package models.events
import models.identifiers.*
import utility.Measurement
import java.lang.reflect.Type
class EventProduct {
    var EPC: EPC = EPC()
    var Quantity: Measurement = Measurement()
    var Type: EventProductType = EventProductType()
    companion object{
    }
}
