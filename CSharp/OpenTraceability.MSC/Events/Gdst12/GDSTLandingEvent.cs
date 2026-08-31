using OpenTraceability.GDST.Events;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.MSC.Events
{
    /// <summary>
    /// A landing event as defined by GDST 1.2: OBJECT / OBSERVE / business step "urn:gdst:bizStep:landing".
    /// GDST 2.0 expresses a landing as a receiving event whose source is a vessel and whose destination is a
    /// land facility, resolved from the location classifications in the master data.
    /// </summary>
    public class GDSTLandingEvent : ObjectEvent<GDSTILMD>, IGDSTILMDEvent
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

        public GDSTLandingEvent()
        {
            this.BusinessStep = new Uri("urn:gdst:bizStep:landing");
            this.Action = EventAction.OBSERVE;
        }
    }
}
