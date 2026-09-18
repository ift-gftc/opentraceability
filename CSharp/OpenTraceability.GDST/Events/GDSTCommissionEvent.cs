using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.GDST.Events
{
    public class GDSTCommissionEvent : ObjectEvent<GDSTILMD>, IGDSTILMDEvent, IGDSTProductOwnerEvent
    {
        [OpenTraceability(Constants.GDST_NAMESPACE, "productOwner")]
        [OpenTraceabilityJson("gdst:productOwner")]
        public PGLN? ProductOwner { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "humanWelfarePolicy")]
        [OpenTraceabilityJson("gdst:humanWelfarePolicy")]
        public string? HumanWelfarePolicy { get; set; }

        public GDSTCommissionEvent()
        {
            BusinessStep = new Uri("urn:epcglobal:cbv:bizstep:commissioning");
            Action = EventAction.ADD;
            Disposition = new Uri("active", UriKind.RelativeOrAbsolute);
        }
    }
}
