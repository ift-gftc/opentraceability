using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.Models.Common
{
    public class SBDHDocumentIdentification
    {
        [OpenTraceability(Constants.SBDH_NAMESPACE, "Standard", 1)]
        public string Standard { get; set; } = string.Empty;

        [OpenTraceability(Constants.SBDH_NAMESPACE, "TypeVersion", 2)]
        public string TypeVersion { get; set; } = string.Empty;

        [OpenTraceability(Constants.SBDH_NAMESPACE, "InstanceIdentifier", 3)]
        public string InstanceIdentifier { get; set; } = string.Empty;

        [OpenTraceability(Constants.SBDH_NAMESPACE, "Type", 4)]
        public string Type { get; set; } = string.Empty;

        [OpenTraceability(Constants.SBDH_NAMESPACE, "MultipleType", 5)]
        public string MultipleType { get; set; } = string.Empty;

        [OpenTraceability(Constants.SBDH_NAMESPACE, "CreationDateAndTime", 6)]
        public DateTimeOffset? CreationDateAndTime { get; set; }
    }
}