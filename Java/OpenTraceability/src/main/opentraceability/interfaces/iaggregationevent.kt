package interfaces
import models.identifiers.*
interface IAggregationEvent {
    fun get_ParentID(): EPC
    fun set_ParentID(value: EPC): Void
}
