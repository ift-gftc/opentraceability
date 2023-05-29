package opentraceability.interfaces

interface IILMDEvent<T> : IEvent {
    var ILMD: T?
}
