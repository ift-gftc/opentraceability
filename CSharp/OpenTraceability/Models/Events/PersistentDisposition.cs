using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.Models.Events
{
    public class PersistentDisposition
    {
        [OpenTraceability("unset", 1)]
        [OpenTraceabilityArray]
        public List<Uri>? Unset { get; set; }

        [OpenTraceability("set", 2)]
        [OpenTraceabilityArray]
        public List<Uri>? Set { get; set; }
    }
}