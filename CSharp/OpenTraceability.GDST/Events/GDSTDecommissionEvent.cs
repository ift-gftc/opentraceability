using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.GDST.Events
{
    public class GDSTDecommissionEvent : ObjectEvent<GDSTILMD>, IGDSTProductOwnerEvent
    {
        [OpenTraceabilityJson("gdst:productOwner")]
        public PGLN? ProductOwner { get; set; }

        public GDSTDecommissionEvent()
        {
            BusinessStep = new Uri("urn:epcglobal:cbv:bizstep:destroying");
            Action = EventAction.DELETE;
        }
    }
}
