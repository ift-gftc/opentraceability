using System;
using OpenTraceability.Models.Identifiers;

namespace OpenTraceability.Interfaces
{
    public interface IAggregationEvent : IEvent
    {
        EPC? ParentID { get; set; }
    }
}