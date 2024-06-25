using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.MSC.Events;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.MSC.Events
{
    public class MSCStorageEvent : ObjectEvent<MSCILMD>, IMSCILMDEvent
    {
        public MSCStorageEvent()
        {
            this.BusinessStep = new Uri("urn:epcglobal:cbv:bizstep:storing");
            this.Action = EventAction.OBSERVE;
        }

        [OpenTraceability(Constants.GDST_NAMESPACE, "productOwner")]
        [OpenTraceabilityJson("gdst:productOwner")]
        public PGLN? ProductOwner { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "humanWelfarePolicy")]
        [OpenTraceabilityJson("gdst:humanWelfarePolicy")]
        public string? HumanWelfarePolicy { get; set; }
    }
}