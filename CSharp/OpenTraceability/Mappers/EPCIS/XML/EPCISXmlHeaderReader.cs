using OpenTraceability.Models.Common;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;

namespace OpenTraceability.Mappers.EPCIS.XML
{
    public static class EPCISXmlHeaderReader
    {
        public static StandardBusinessDocumentHeader ReadHeader(XElement x)
        {
            StandardBusinessDocumentHeader header = new StandardBusinessDocumentHeader();
            header.HeaderVersion = x.Element(Constants.SBDH_XNAMESPACE + "HeaderVersion")?.Value ?? string.Empty;

            XElement? xSender = x.Element(Constants.SBDH_XNAMESPACE + "Sender");
            if (xSender != null)
            {
                header.Sender = ReadHeaderOrganization(xSender);
            }

            XElement? xReceiver = x.Element(Constants.SBDH_XNAMESPACE + "Receiver");
            if (xReceiver != null)
            {
                header.Receiver = ReadHeaderOrganization(xReceiver);
            }

            XElement? xDocId = x.Element(Constants.SBDH_XNAMESPACE + "DocumentIdentification");
            if (xDocId != null)
            {
                header.DocumentIdentification = new SBDHDocumentIdentification();
                header.DocumentIdentification.Standard = xDocId.Element(Constants.SBDH_XNAMESPACE + "Standard")?.Value ?? string.Empty;
                header.DocumentIdentification.Type = xDocId.Element(Constants.SBDH_XNAMESPACE + "Type")?.Value ?? string.Empty;
                header.DocumentIdentification.TypeVersion = xDocId.Element(Constants.SBDH_XNAMESPACE + "TypeVersion")?.Value ?? string.Empty;
                header.DocumentIdentification.InstanceIdentifier = xDocId.Element(Constants.SBDH_XNAMESPACE + "InstanceIdentifier")?.Value ?? string.Empty;
                header.DocumentIdentification.MultipleType = xDocId.Element(Constants.SBDH_XNAMESPACE + "MultipleType")?.Value ?? string.Empty;

                if (DateTimeOffset.TryParse(xDocId.Element(Constants.SBDH_XNAMESPACE + "CreationDateAndTime")?.Value, out DateTimeOffset dt))
                {
                    header.DocumentIdentification.CreationDateAndTime = dt;
                }
            }

            return header;
        }

        private static SBDHOrganization ReadHeaderOrganization(XElement x)
        {
            SBDHOrganization org = new SBDHOrganization();
            org.Identifier = x.Element(Constants.SBDH_XNAMESPACE + "Identifier")?.Value ?? string.Empty;

            XElement? xContactInformation = x.Element(Constants.SBDH_XNAMESPACE + "ContactInformation");
            if (xContactInformation != null)
            {
                org.ContactName = xContactInformation.Element(Constants.SBDH_XNAMESPACE + "Contact")?.Value ?? string.Empty;
                org.EmailAddress = xContactInformation.Element(Constants.SBDH_XNAMESPACE + "EmailAddress")?.Value ?? string.Empty;
            }

            return org;
        }
    }
}
