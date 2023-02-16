using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.Models.Common
{
    public class StandardBusinessDocumentHeader
    {
        [OpenTraceability(Constants.SBDH_NAMESPACE, "HeaderVersion")]
        public string HeaderVersion { get; set; } = string.Empty;

        [OpenTraceabilityObject]
        [OpenTraceability(Constants.SBDH_NAMESPACE, "Sender")]
        public SBDHOrganization? Sender { get; set; }

        [OpenTraceabilityObject]
        [OpenTraceability(Constants.SBDH_NAMESPACE, "Receiver")]
        public SBDHOrganization? Receiver { get; set; }

        [OpenTraceabilityObject]
        [OpenTraceability(Constants.SBDH_NAMESPACE, "DocumentIdentification")]
        public SBDHDocumentIdentification? DocumentIdentification { get; set; }
    }
}