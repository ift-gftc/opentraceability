package opentraceability.interfaces

import opentraceability.models.identifiers.*

interface IAggregationEvent : IEvent {

    var parentID: EPC?
}
