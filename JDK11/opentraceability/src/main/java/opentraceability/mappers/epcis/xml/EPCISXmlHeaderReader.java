package opentraceability.mappers.epcis.xml;

import opentraceability.models.common.*;
import opentraceability.*;
import opentraceability.mappers.*;
import opentraceability.mappers.epcis.*;

public final class EPCISXmlHeaderReader
{
	public static StandardBusinessDocumentHeader ReadHeader(XElement x)
	{
		StandardBusinessDocumentHeader header = new StandardBusinessDocumentHeader();
		header.setHeaderVersion(x.Element(Constants.SBDH_XNAMESPACE + "HeaderVersion") == null ? null : ((x.Element(Constants.SBDH_XNAMESPACE + "HeaderVersion").Value) != null ? x.Element(Constants.SBDH_XNAMESPACE + "HeaderVersion").Value : ""));

		XElement xSender = x.Element(Constants.SBDH_XNAMESPACE + "Sender");
		if (xSender != null)
		{
			header.setSender(ReadHeaderOrganization(xSender));
		}

		XElement xReceiver = x.Element(Constants.SBDH_XNAMESPACE + "Receiver");
		if (xReceiver != null)
		{
			header.setReceiver(ReadHeaderOrganization(xReceiver));
		}

		XElement xDocId = x.Element(Constants.SBDH_XNAMESPACE + "DocumentIdentification");
		if (xDocId != null)
		{
			header.setDocumentIdentification(new SBDHDocumentIdentification());
			header.DocumentIdentification.setStandard(xDocId.Element(Constants.SBDH_XNAMESPACE + "Standard") == null ? null : ((xDocId.Element(Constants.SBDH_XNAMESPACE + "Standard").Value) != null ? xDocId.Element(Constants.SBDH_XNAMESPACE + "Standard").Value : ""));
			header.DocumentIdentification.setType(xDocId.Element(Constants.SBDH_XNAMESPACE + "Type") == null ? null : ((xDocId.Element(Constants.SBDH_XNAMESPACE + "Type").Value) != null ? xDocId.Element(Constants.SBDH_XNAMESPACE + "Type").Value : ""));
			header.DocumentIdentification.setTypeVersion(xDocId.Element(Constants.SBDH_XNAMESPACE + "TypeVersion") == null ? null : ((xDocId.Element(Constants.SBDH_XNAMESPACE + "TypeVersion").Value) != null ? xDocId.Element(Constants.SBDH_XNAMESPACE + "TypeVersion").Value : ""));
			header.DocumentIdentification.setInstanceIdentifier(xDocId.Element(Constants.SBDH_XNAMESPACE + "InstanceIdentifier") == null ? null : ((xDocId.Element(Constants.SBDH_XNAMESPACE + "InstanceIdentifier").Value) != null ? xDocId.Element(Constants.SBDH_XNAMESPACE + "InstanceIdentifier").Value : ""));
			header.DocumentIdentification.setMultipleType(xDocId.Element(Constants.SBDH_XNAMESPACE + "MultipleType") == null ? null : ((xDocId.Element(Constants.SBDH_XNAMESPACE + "MultipleType").Value) != null ? xDocId.Element(Constants.SBDH_XNAMESPACE + "MultipleType").Value : ""));

			OffsetDateTime dt = new OffsetDateTime();
			tangible.OutObject<System.OffsetDateTime> tempOut_dt = new tangible.OutObject<System.OffsetDateTime>();
			if (OffsetDateTime.TryParse((xDocId.Element(Constants.SBDH_XNAMESPACE + "CreationDateAndTime") == null ? null : xDocId.Element(Constants.SBDH_XNAMESPACE + "CreationDateAndTime").Value), tempOut_dt))
			{
			dt = tempOut_dt.outArgValue;
				header.DocumentIdentification.setCreationDateAndTime(dt);
			}
		else
		{
			dt = tempOut_dt.outArgValue;
		}
		}

		return header;
	}

	private static SBDHOrganization ReadHeaderOrganization(XElement x)
	{
		SBDHOrganization org = new SBDHOrganization();
		org.setIdentifier(x.Element(Constants.SBDH_XNAMESPACE + "Identifier") == null ? null : ((x.Element(Constants.SBDH_XNAMESPACE + "Identifier").Value) != null ? x.Element(Constants.SBDH_XNAMESPACE + "Identifier").Value : ""));

		XElement xContactInformation = x.Element(Constants.SBDH_XNAMESPACE + "ContactInformation");
		if (xContactInformation != null)
		{
			org.ContactName = xContactInformation.Element(Constants.SBDH_XNAMESPACE + "Contact") == null ? null : ((xContactInformation.Element(Constants.SBDH_XNAMESPACE + "Contact").Value) != null ? xContactInformation.Element(Constants.SBDH_XNAMESPACE + "Contact").Value : "");
			org.EmailAddress = xContactInformation.Element(Constants.SBDH_XNAMESPACE + "EmailAddress") == null ? null : ((xContactInformation.Element(Constants.SBDH_XNAMESPACE + "EmailAddress").Value) != null ? xContactInformation.Element(Constants.SBDH_XNAMESPACE + "EmailAddress").Value : "");
		}

		return org;
	}
}
