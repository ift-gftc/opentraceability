using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.Models.Events
{
    public class PersistentDisposition
    {
        [OpenTraceabilityXml("unset", 1)]
        [OpenTraceabilityArray]
        public List<Uri>? Unset { get; set; }

        [OpenTraceabilityXml("set", 2)]
        [OpenTraceabilityArray]
        public List<Uri>? Set { get; set; }
    }
}