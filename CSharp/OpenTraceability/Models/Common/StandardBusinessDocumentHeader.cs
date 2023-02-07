namespace OpenTraceability.Models.Common
{
    public class StandardBusinessDocumentHeader
    {
        public string HeaderVersion { get; set; } = string.Empty;
        public SBDHOrganization? Sender { get; set; }
        public SBDHOrganization? Receiver { get; set; }
        public SBDHDocumentIdentification? DocumentIdentification { get; set; }
    }
}