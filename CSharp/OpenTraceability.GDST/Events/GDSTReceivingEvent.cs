using OpenTraceability.Models.Events;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.GDST.Events
{
    public class GDSTReceivingEvent : ObjectEvent<GDSTILMD>, IGDSTILMDEvent
    {
        [OpenTraceabilityJson("gdst:humanWelfarePolicy")]
        public string? HumanWelfarePolicy { get; set; }

        [OpenTraceabilityJson("cbvmda:unloadingPort")]
        public string? UnloadingPort { get; set; }

        public GDSTReceivingEvent()
        {
            BusinessStep = new Uri("urn:epcglobal:cbv:bizstep:receiving");
            Action = EventAction.OBSERVE;
            Disposition = new Uri("arrived", UriKind.RelativeOrAbsolute);
        }
    }
}
