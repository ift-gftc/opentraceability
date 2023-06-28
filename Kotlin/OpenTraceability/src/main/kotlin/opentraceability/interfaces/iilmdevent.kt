package opentraceability.interfaces

interface IILMDEvent<T> : IEvent {
    var ilmd: T?
}
