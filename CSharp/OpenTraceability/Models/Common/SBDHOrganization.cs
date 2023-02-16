using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.Models.Common
{
    public class SBDHOrganization
    {
        [OpenTraceability(Constants.SBDH_NAMESPACE, "Identifier")]
        public string Identifier { get; set; } = string.Empty;

        [OpenTraceabilityObject]
        [OpenTraceability(Constants.SBDH_NAMESPACE, "ContactInformation")]
        public SBDHContact ContactInformation { get; set; } = new SBDHContact();
    }

    public class SBDHContact
    {
        [OpenTraceability(Constants.SBDH_NAMESPACE, "Contact")]
        public string ContactName { get; set; } = string.Empty;

        [OpenTraceability(Constants.SBDH_NAMESPACE, "EmailAddress")]
        public string EmailAddress { get; set; } = string.Empty;
    }
}