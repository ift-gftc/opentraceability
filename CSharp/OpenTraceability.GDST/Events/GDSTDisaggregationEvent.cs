using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.GDST.Events
{
    public class GDSTDisaggregationEvent : AggregationEvent<GDSTILMD>, IGDSTProductOwnerEvent
    {
        [OpenTraceabilityJson("gdst:productOwner")]
        public PGLN? ProductOwner { get; set; }

        public GDSTDisaggregationEvent()
        {
            BusinessStep = new Uri("urn:epcglobal:cbv:bizstep:unpacking");
            Action = EventAction.DELETE;
        }
    }
}
