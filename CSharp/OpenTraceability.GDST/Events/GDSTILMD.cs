using OpenTraceability.GDST.Events.KDEs;
using OpenTraceability.Models.Events;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.GDST.Events
{
    public class GDSTILMD : EventILMD
    {
        [OpenTraceabilityObject]
        [OpenTraceabilityJson("cbvmda:vesselCatchInformationList")]
        public VesselCatchInformationList? VesselCatchInformationList { get; set; }

        [OpenTraceabilityJson("gdst:broodstockSource")]
        public string? BroodstockSource { get; set; }

        [OpenTraceabilityJson("gdst:aquacultureMethod")]
        public string? AquacultureMethod { get; set; }

        [OpenTraceabilityJson("gdst:proteinSource")]
        public string? ProteinSource { get; set; }
    }
}
