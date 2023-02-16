using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.Models.Events
{
    public class EventReadPoint
    {
        [OpenTraceability("id")]
        public Uri? ID { get; set; }
    }
}