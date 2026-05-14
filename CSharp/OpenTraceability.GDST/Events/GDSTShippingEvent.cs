using OpenTraceability.Models.Events;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.GDST.Events
{
    public class GDSTShippingEvent : ObjectEvent<GDSTILMD>, IGDSTILMDEvent
    {
        [OpenTraceabilityJson("cbvmda:unloadingPort")]
        public string? UnloadingPort { get; set; }

        public GDSTShippingEvent()
        {
            BusinessStep = new Uri("urn:epcglobal:cbv:bizstep:shipping");
            Action = EventAction.OBSERVE;
            Disposition = new Uri("in_transit", UriKind.RelativeOrAbsolute);
        }
    }
}
