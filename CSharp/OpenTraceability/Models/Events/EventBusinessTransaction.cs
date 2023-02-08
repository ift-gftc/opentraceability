namespace OpenTraceability.Models.Events
{
    public enum EventBusinessTransactionType
    {
        // TODO: add in the default business transaction types
    }

    public class EventBusinessTransaction
    {
        public string RawType { get; set; } = string.Empty;
        public EventBusinessTransactionType Type { get; set; }
        public string? Value { get; set; }
    }
}