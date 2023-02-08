using OpenTraceability.Models.Common;
using OpenTraceability.Utility;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;

namespace OpenTraceability.Mappers.EPCIS.XML
{
    public static class EPCISXmlHeaderWriter
    {
        public static XElement WriteHeader(StandardBusinessDocumentHeader header)
        {
            XElement xHeader = new XElement(Constants.SBDH_XNAMESPACE + "StandardBusinessDocumentHeader");

            if (!string.IsNullOrWhiteSpace(header.HeaderVersion))
            {
                xHeader.Add(new XElement(Constants.SBDH_XNAMESPACE + "HeaderVersion", header.HeaderVersion));
            }

            WriteHeaderOrganization("Sender", header.Sender, xHeader);
            WriteHeaderOrganization("Receiver", header.Receiver, xHeader);

            if (header.DocumentIdentification != null)
            {
                XElement xDocId = new XElement(Constants.SBDH_XNAMESPACE + "DocumentIdentification");
                xDocId.AddStringElement(Constants.SBDH_XNAMESPACE + "Standard", header.DocumentIdentification.Standard);
                xDocId.AddStringElement(Constants.SBDH_XNAMESPACE + "Type", header.DocumentIdentification.Type);
                xDocId.AddStringElement(Constants.SBDH_XNAMESPACE + "TypeVersion", header.DocumentIdentification.TypeVersion);
                xDocId.AddStringElement(Constants.SBDH_XNAMESPACE + "InstanceIdentifier", header.DocumentIdentification.InstanceIdentifier);
                xDocId.AddStringElement(Constants.SBDH_XNAMESPACE + "MultipleType", header.DocumentIdentification.MultipleType);
                xDocId.AddDateTimeISOElement(Constants.SBDH_XNAMESPACE + "CreationDateAndTime", header.DocumentIdentification.CreationDateAndTime);
            }

            return xHeader;
        }

        private static void WriteHeaderOrganization(string xName, SBDHOrganization? org, XElement xHeader)
        {
            if (org != null)
            {
                XElement xOrg = new XElement(Constants.SBDH_XNAMESPACE + xName);
                if (!string.IsNullOrWhiteSpace(org.Identifier))
                {
                    xOrg.Add(Constants.SBDH_XNAMESPACE + "Identifier", org.Identifier);
                }
                if (!string.IsNullOrWhiteSpace(org.ContactName) || !string.IsNullOrWhiteSpace(org.EmailAddress))
                {
                    XElement xContactInfo = new XElement(Constants.SBDH_NAMESPACE + "ContactInformation");
                    if (!string.IsNullOrWhiteSpace(org.ContactName))
                    {
                        xContactInfo.Add(new XElement(Constants.SBDH_NAMESPACE + "Contact"), org.ContactName);
                    }
                    if (!string.IsNullOrWhiteSpace(org.EmailAddress))
                    {
                        xContactInfo.Add(new XElement(Constants.SBDH_NAMESPACE + "EmailAddress"), org.EmailAddress);
                    }
                }
                xHeader.Add(xOrg);
            }
        }
    }
}
