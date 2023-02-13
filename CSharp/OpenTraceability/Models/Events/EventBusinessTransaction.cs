using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.Models.Events
{
    public enum EventBusinessTransactionType
    {
        // TODO: add in the default business transaction types
    }

    [OpenTraceabilityXml("bizTransaction")]
    public class EventBusinessTransaction
    {
        [OpenTraceabilityXml("@type")]
        public string RawType { get; set; } = string.Empty;

        [OpenTraceabilityXml("text()")]
        public string? Value { get; set; }

        public EventBusinessTransactionType Type { get; set; }
    }
}