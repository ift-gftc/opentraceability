using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.GDST.Events.KDEs
{
    public class VesselCatchInformationList
    {
        [OpenTraceabilityArray]
        [OpenTraceabilityObject]
        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "vesselCatchInformation")]
        [OpenTraceabilityJson("cbvmda:vesselCatchInformation")]
        public List<VesselCatchInformation> Vessels { get; set; } = new List<VesselCatchInformation>();
    }
}
