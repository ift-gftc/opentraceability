using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.GDST.Events
{
    public class GDSTAggregationEvent : AggregationEvent<GDSTILMD>, IGDSTProductOwnerEvent
    {
        [OpenTraceability(Constants.GDST_NAMESPACE, "productOwner")]
        [OpenTraceabilityJson("gdst:productOwner")]
        public PGLN? ProductOwner { get; set; }

        public GDSTAggregationEvent()
        {
            BusinessStep = new Uri("urn:epcglobal:cbv:bizstep:packing");
            Action = EventAction.ADD;
            Disposition = new Uri("active", UriKind.RelativeOrAbsolute);
        }
    }
}
