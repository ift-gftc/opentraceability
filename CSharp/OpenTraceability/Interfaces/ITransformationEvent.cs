using System;

namespace OpenTraceability.Interfaces
{
    public interface ITransformationEvent : IEvent
    {
        string? TransformationID { get; set; }
    }
}