using OpenTraceability.Interfaces;
using OpenTraceability.Models.Identifiers;

namespace OpenTraceability.GDST.Events
{
    public interface IGDSTEvent : IEvent
    {
        PGLN InformationProvider { get; set; }
    }

    public interface IGDSTProductOwnerEvent : IGDSTEvent
    {
        PGLN? ProductOwner { get; set; }
    }
}
