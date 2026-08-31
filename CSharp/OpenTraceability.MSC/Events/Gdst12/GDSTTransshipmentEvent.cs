using OpenTraceability.GDST.Events;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.MSC.Events
{
    /// <summary>
    /// A transshipment event as defined by GDST 1.2: OBJECT / OBSERVE / business step "urn:gdst:bizStep:transshipment".
    /// GDST 2.0 models a transshipment as a shipping and a receiving event instead, so this single event has no direct
    /// 2.0 equivalent. It keeps productOwner and humanWelfarePolicy, which GDST 1.2 carried on the event itself.
    /// </summary>
    public class GDSTTransshipmentEvent : ObjectEvent<GDSTILMD>, IGDSTILMDEvent
    {
        [OpenTraceability(Constants.GDST_NAMESPACE, "productOwner")]
        [OpenTraceabilityJson("gdst:productOwner")]
        public PGLN? ProductOwner { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "humanWelfarePolicy")]
        [OpenTraceabilityJson("gdst:humanWelfarePolicy")]
        public string? HumanWelfarePolicy { get; set; }

        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "unloadingPort")]
        [OpenTraceabilityJson("cbvmda:unloadingPort")]
        public string? UnloadingPort { get; set; }

        public GDSTTransshipmentEvent()
        {
            this.BusinessStep = new Uri("urn:gdst:bizStep:transshipment");
            this.Action = EventAction.OBSERVE;
        }
    }
}
