package interfaces
import models.identifiers.*

interface IAggregationEvent : IEvent {

    var ParentID: EPC?
}
