using OpenTraceability.Models.Events;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.GDST.Events
{
    public class GDSTReceivingEvent : ObjectEvent<GDSTILMD>, IGDSTILMDEvent
    {
        [OpenTraceability(Constants.GDST_NAMESPACE, "humanWelfarePolicy")]
        [OpenTraceabilityJson("gdst:humanWelfarePolicy")]
        public string? HumanWelfarePolicy { get; set; }

        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "unloadingPort")]
        [OpenTraceabilityJson("cbvmda:unloadingPort")]
        public string? UnloadingPort { get; set; }

        public GDSTReceivingEvent()
        {
            BusinessStep = new Uri("urn:epcglobal:cbv:bizstep:receiving");
            Action = EventAction.OBSERVE;
        }
    }
}
