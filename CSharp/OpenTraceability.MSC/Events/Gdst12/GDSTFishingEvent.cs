using OpenTraceability.GDST.Events;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.MSC.Events
{
    /// <summary>
    /// A fishing event as defined by GDST 1.2: OBJECT / ADD / business step "urn:gdst:bizStep:fishingEvent".
    /// GDST 2.0 replaced it with <see cref="GDSTCommissionEvent"/>, which derives the profile from the master data
    /// classifications instead of a GDST specific business step. MSC still reads GDST 1.2 solutions, so the class
    /// lives here now that the GDST package ships 2.0 only.
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
