using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using OpenTraceability.Models.Events.KDEs;
using OpenTraceability.Utility.Attributes;
using System.Xml.Linq;

namespace OpenTraceability.GDST.Events.KDEs
{
    public class VesselCatchInformationList
    {
        [OpenTraceabilityArray]
        [OpenTraceabilityObject]
        [OpenTraceabilityJson("cbvmda:vesselCatchInformation")]
        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "vesselCatchInformation", 1)]
        public List<VesselCatchInformation> Vessels { get; set; } = new List<VesselCatchInformation>();
    }
}