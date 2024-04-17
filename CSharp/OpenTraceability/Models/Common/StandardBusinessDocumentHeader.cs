using OpenTraceability.Utility.Attributes;
using System;

namespace OpenTraceability.Models.Common
{
    public class StandardBusinessDocumentHeader
    {
        [OpenTraceability(Constants.SBDH_NAMESPACE, "HeaderVersion")]
        public string HeaderVersion { get; set; } = string.Empty;

        [OpenTraceabilityObject]
        [OpenTraceability(Constants.SBDH_NAMESPACE, "Sender")]
        public SBDHOrganization Sender { get; set; }

        [OpenTraceabilityObject]
        [OpenTraceability(Constants.SBDH_NAMESPACE, "Receiver")]
        public SBDHOrganization Receiver { get; set; }

        [OpenTraceabilityObject]
        [OpenTraceability(Constants.SBDH_NAMESPACE, "DocumentIdentification")]
        public SBDHDocumentIdentification DocumentIdentification { get; set; }

        public static StandardBusinessDocumentHeader DummyHeader
        {
            get
            {
                var dh = new StandardBusinessDocumentHeader();
                dh.HeaderVersion = "x";
                dh.Sender = new SBDHOrganization()
                {
                    Identifier = "x",
                    ContactInformation = new SBDHContact()
                    {
                        ContactName = "x",
                        EmailAddress = "x"
                    }
                };
                dh.Receiver = new SBDHOrganization()
                {
                    Identifier = "x",
                    ContactInformation = new SBDHContact()
                    {
                        ContactName = "x",
                        EmailAddress = "x"
                    }
                };
                dh.DocumentIdentification = new SBDHDocumentIdentification()
                {
                    CreationDateAndTime = DateTime.UtcNow,
                    InstanceIdentifier = "x",
                    MultipleType = "false",
                    Standard = "x",
                    Type = "x",
                    TypeVersion = "3.0"
                };
                return dh;
            }
        }
    }
}