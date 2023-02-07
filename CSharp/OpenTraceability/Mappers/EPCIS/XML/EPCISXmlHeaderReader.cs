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
        public static StandardBusinessDocumentHeader ReadHeader(XElement x, string headerNS)
        {
            StandardBusinessDocumentHeader header = new StandardBusinessDocumentHeader();
            header.HeaderVersion = x.Element($"{headerNS}:HeaderVersion")?.Value ?? string.Empty;

            XElement? xSender = x.Element($"{headerNS}:Sender");
            if (xSender != null)
            {
                header.Sender = ReadHeaderOrganization(xSender, headerNS);
            }

            XElement? xReceiver = x.Element($"{headerNS}:Receiver");
            if (xReceiver != null)
            {
                header.Receiver = ReadHeaderOrganization(xReceiver, headerNS);
            }

            XElement? xDocId = x.Element($"{headerNS}:DocumentIdentification");
            if (xDocId != null)
            {
                header.DocumentIdentification = new SBDHDocumentIdentification();
                header.DocumentIdentification.Standard = xDocId.Element($"{headerNS}:Standard")?.Value ?? string.Empty;
                header.DocumentIdentification.Type = xDocId.Element($"{headerNS}:Type")?.Value ?? string.Empty;
                header.DocumentIdentification.TypeVersion = xDocId.Element($"{headerNS}:TypeVersion")?.Value ?? string.Empty;
                header.DocumentIdentification.InstanceIdentifier = xDocId.Element($"{headerNS}:InstanceIdentifier")?.Value ?? string.Empty;
                header.DocumentIdentification.MultipleType = xDocId.Element($"{headerNS}:MultipleType")?.Value ?? string.Empty;

                if (DateTimeOffset.TryParse(xDocId.Element($"{headerNS}:CreationDateAndTime")?.Value, out DateTimeOffset dt))
                {
                    header.DocumentIdentification.CreationDateAndTime = dt;
                }
            }

            return header;
        }

        private static SBDHOrganization ReadHeaderOrganization(XElement x, string headerNS)
        {
            SBDHOrganization org = new SBDHOrganization();
            org.Identifier = x.Element($"{headerNS}:Identifier")?.Value ?? string.Empty;

            XElement? xContactInformation = x.Element($"{headerNS}:ContainInformation");
            if (xContactInformation != null)
            {
                org.ContactName = xContactInformation.Element($"{headerNS}:Contact")?.Value ?? string.Empty;
                org.EmailAddress = xContactInformation.Element($"{headerNS}:EmailAddress")?.Value ?? string.Empty;
            }

            return org;
        }
    }
}
