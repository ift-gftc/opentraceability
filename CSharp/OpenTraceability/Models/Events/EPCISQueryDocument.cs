using OpenTraceability.Interfaces;
using OpenTraceability.Models.Common;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.Models.Events
{
    public class EPCISQueryDocument : EPCISBaseDocument
    {

        public string QueryName { get; set; } = string.Empty;
    }
}