using OpenTraceability.GDST.Events;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.MSC.Events
{
    /// <summary>
    /// ILMD extension for GDST KDEs.
    /// </summary>
    public class MSCILMD : GDSTILMD
    {
        [OpenTraceability(Constants.GDST_NAMESPACE, "processingType")]
        [OpenTraceabilityJson("gdst:processingType")]
        public string? ProcessingType { get; set; }
    }
}