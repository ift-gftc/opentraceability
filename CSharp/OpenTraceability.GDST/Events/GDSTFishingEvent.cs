using OpenTraceability.GDST.Events.KDEs;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.GDST.Events
{
    /// <summary>
    /// Represents a fishing event that follows the GDST 1.2 seafood traceability standard. The event profile for this event is
    /// that it is OBJECT - ADD - with business step "urn:gdst:bizStep:fishingEvent".
    /// </summary>
    public class GDSTFishingEvent : ObjectEvent<GDSTILMD>, IGDSTILMDEvent
    {
        [OpenTraceability(Constants.GDST_NAMESPACE, "productOwner")]
        [OpenTraceabilityJson("gdst:productOwner")]
        public PGLN? ProductOwner { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "humanWelfarePolicy")]
        [OpenTraceabilityJson("gdst:humanWelfarePolicy")]
        public string? HumanWelfarePolicy { get; set; }

        public GDSTFishingEvent()
        {
            this.BusinessStep = new Uri("urn:gdst:bizStep:fishingEvent");
            this.Action = EventAction.ADD;
        }
    }
}