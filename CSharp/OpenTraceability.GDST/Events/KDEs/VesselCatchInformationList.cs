using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.GDST.Events.KDEs
{
    public class VesselCatchInformationList
    {
        [OpenTraceabilityArray]
        [OpenTraceabilityObject]
        [OpenTraceabilityJson("cbvmda:vesselCatchInformation")]
        public List<VesselCatchInformation> Vessels { get; set; } = new List<VesselCatchInformation>();
    }
}
