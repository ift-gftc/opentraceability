namespace OpenTraceability.Models.Common
{
    public class Certificate
    {
        public string? CertificateType { get; set; }
        public string? Agency { get; set; }
        public string? Standard { get; set; }
        public string? Value { get; set; }
        public string? Identification { get; set; }
        public DateTimeOffset? StartDate { get; set; }
        public DateTimeOffset? EndDate { get; set; }
        public Uri? URI { get; set; }
        public Uri? AgencyURL { get; set; }
        public DateTimeOffset? AuditDate { get; set; }
        public string? Subject { get; set; }
    }
}