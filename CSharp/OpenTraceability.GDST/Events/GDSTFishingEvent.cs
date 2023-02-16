using OpenTraceability.GDST.Events.KDEs;
using OpenTraceability.Models.Events;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.GDST.Events
{
    /// <summary>
    /// Represents a fishing event that follows the GDST 1.2 seafood traceability standard. The event profile for this event is
    /// that it is OBJECT - ADD - with business step "urn:gdst:bizStep:fishingEvent".
    /// </summary>
    public class GDSTFishingEvent : ObjectEvent<GDSTILMD>
    {
        public GDSTFishingEvent()
        {

        }
    }
}