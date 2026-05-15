using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.GDST.Events
{
    public class GDSTTransformationEvent : TransformationEvent<GDSTILMD>, IGDSTILMDEvent, IGDSTProductOwnerEvent
    {
        [OpenTraceabilityJson("gdst:productOwner")]
        public PGLN? ProductOwner { get; set; }

        [OpenTraceabilityJson("gdst:humanWelfarePolicy")]
        public string? HumanWelfarePolicy { get; set; }

        public GDSTTransformationEvent()
        {
            BusinessStep = new Uri("urn:epcglobal:cbv:bizstep:commissioning");
        }
    }
}
