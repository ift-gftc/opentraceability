namespace OpenTraceability.Models.Common
{
    public class SBDHDocumentIdentification
    {
        public string Standard { get; set; } = string.Empty;
        public string TypeVersion { get; set; } = string.Empty;
        public string InstanceIdentifier { get; set; } = string.Empty;
        public string Type { get; set; } = string.Empty;
        public string MultipleType { get; set; } = string.Empty;
        public DateTimeOffset? CreationDateAndTime { get; set; }
    }
}