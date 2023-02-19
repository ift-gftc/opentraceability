using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.Models.Events
{
    public enum EventBusinessTransactionType
    {
        // TODO: add in the default business transaction types
    }

    public class EventBusinessTransaction
    {
        [OpenTraceabilityJson("type")]
        [OpenTraceability("@type")]
        public string RawType { get; set; } = string.Empty;

        [OpenTraceabilityJson("bizTransaction")]
        [OpenTraceability("text()")]
        public string? Value { get; set; }

        public EventBusinessTransactionType Type { get; set; }
    }
}