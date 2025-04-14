using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.MSC.Events;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.MSC.Events
{
    public class MSCShippingEvent : ObjectEvent<MSCILMD>, IMSCILMDEvent
    {
        public MSCShippingEvent()
        {
            this.BusinessStep = new Uri("urn:epcglobal:cbv:bizstep:shipping");
            this.Action = EventAction.OBSERVE;
        }

        [OpenTraceability(Constants.GDST_NAMESPACE, "productOwner")]
        [OpenTraceabilityJson("gdst:productOwner")]
        public PGLN? ProductOwner { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "humanWelfarePolicy")]
        [OpenTraceabilityJson("gdst:humanWelfarePolicy")]
        public string? HumanWelfarePolicy { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "transportType")]
        [OpenTraceabilityJson("gdst:transportType")]
        public string? TransportType { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "transportVehicleID")]
        [OpenTraceabilityJson("gdst:transportVehicleID")]
        public string? TransportVehicleID { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "transportNumber")]
        [OpenTraceabilityJson("gdst:transportNumber")]
        public string? TransportNumber { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "transportProviderID")]
        [OpenTraceabilityJson("gdst:transportProviderID")]
        public string? TransportProviderID { get; set; }
    }
}