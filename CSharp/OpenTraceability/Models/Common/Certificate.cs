using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.Models.Common
{
    public class Certificate
    {
        [OpenTraceabilityJson("gdst:certificationType")]
        [OpenTraceability(Constants.GDST_NAMESPACE, "certificationType")]
        public string? CertificateType { get; set; }

        [OpenTraceability("certificationAgency")]
        public string? Agency { get; set; }

        [OpenTraceability("certificationStandard")]
        public string? Standard { get; set; }

        [OpenTraceability("certificationValue")]
        public string? Value { get; set; }

        [OpenTraceability("certificationIdentification")]
        public string? Identification { get; set; }

        [OpenTraceability("certificationStartDate")]
        public DateTimeOffset? StartDate { get; set; }

        [OpenTraceability("certificationEndDate")]
        public DateTimeOffset? EndDate { get; set; }
    }
}