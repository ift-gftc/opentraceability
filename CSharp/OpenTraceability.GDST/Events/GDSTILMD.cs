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
        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "vesselCatchInformationList", 0)]
        public VesselCatchInformationList? VesselCatchInformationList { get; set; }
    }
}
