using OpenTraceability.GDST.Events;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.MSC.Events
{
    /// <summary>
    /// A hatching event as defined by GDST 1.2: OBJECT / ADD / business step "urn:gdst:bizStep:hatching".
    /// GDST 2.0 expresses it as a commission event on a product classified as developing.
    /// </summary>
    public class GDSTHatchingEvent : ObjectEvent<GDSTILMD>, IGDSTILMDEvent
    {
        [OpenTraceability(Constants.GDST_NAMESPACE, "productOwner")]
        [OpenTraceabilityJson("gdst:productOwner")]
        public PGLN? ProductOwner { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "humanWelfarePolicy")]
        [OpenTraceabilityJson("gdst:humanWelfarePolicy")]
        public string? HumanWelfarePolicy { get; set; }

        public GDSTHatchingEvent()
        {
            this.BusinessStep = new Uri("urn:gdst:bizStep:hatching");
            this.Action = EventAction.ADD;
        }
    }
}
