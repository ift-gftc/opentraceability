using OpenTraceability.GDST.Events.KDEs;
using OpenTraceability.Models.Events;
using OpenTraceability.Utility.Attributes;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.GDST.Events
{
    /// <summary>
    /// ILMD extension for GDST KDEs.
    /// </summary>
    public class GDSTILMD : EventILMD
    {
        [OpenTraceabilityObject]
        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "vesselCatchInformationList")]
        [OpenTraceabilityJson("cbvmda:vesselCatchInformationList")]
        public VesselCatchInformationList? VesselCatchInformationList { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "broodstockSource")]
        [OpenTraceabilityJson("gdst:broodstockSource")]
        public string? BroodstockSource { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "aquacultureMethod")]
        [OpenTraceabilityJson("gdst:aquacultureMethod")]
        public string? AquacultureMethod { get; set; }

        [OpenTraceability(Constants.GDST_NAMESPACE, "proteinSource")]
        [OpenTraceabilityJson("gdst:proteinSource")]
        public string? ProteinSource { get; set; }
    }
}
